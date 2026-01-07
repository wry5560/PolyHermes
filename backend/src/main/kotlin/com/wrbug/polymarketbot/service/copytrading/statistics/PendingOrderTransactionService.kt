package com.wrbug.polymarketbot.service.copytrading.statistics

import com.wrbug.polymarketbot.api.TradeResponse
import com.wrbug.polymarketbot.entity.CopyOrderTracking
import com.wrbug.polymarketbot.entity.CopyTrading
import com.wrbug.polymarketbot.repository.CopyOrderTrackingRepository
import com.wrbug.polymarketbot.repository.SellMatchRecordRepository
import com.wrbug.polymarketbot.service.copytrading.configs.CopyTradingFilterService
import com.wrbug.polymarketbot.service.copytrading.configs.FilterResult
import com.wrbug.polymarketbot.service.common.PolymarketClobService
import com.wrbug.polymarketbot.util.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Pending 订单事务服务
 *
 * 使用 REQUIRES_NEW 事务传播级别，确保仓位检查和 pending 订单保存在独立事务中执行并立即提交。
 * 这样可以防止并发订单绕过仓位限制：
 * - 问题：应用级 Mutex 释放后，事务尚未提交，其他事务看不到已保存的 pending 订单
 * - 解决：使用 REQUIRES_NEW 确保 pending 订单保存后立即提交，对其他事务可见
 */
@Service
open class PendingOrderTransactionService(
    private val copyOrderTrackingRepository: CopyOrderTrackingRepository,
    private val sellMatchRecordRepository: SellMatchRecordRepository,
    private val filterService: CopyTradingFilterService,
    private val clobService: PolymarketClobService
) {
    private val logger = LoggerFactory.getLogger(PendingOrderTransactionService::class.java)

    /**
     * 仓位锁内的订单预处理结果
     */
    sealed class PendingOrderResult {
        /** 过滤检查失败 */
        data class FilterFailed(val filterResult: FilterResult) : PendingOrderResult()
        /** 跳过（各种原因），记录原因以便存入 filtered_order */
        data class Skipped(val reason: String, val filterType: String) : PendingOrderResult()
        /** 成功预占仓位 */
        data class Success(
            val trackingId: Long,
            val filterResult: FilterResult,
            val buyQuantity: BigDecimal,
            val buyPrice: BigDecimal
        ) : PendingOrderResult()
    }

    /**
     * 在独立事务中执行仓位检查和保存 pending 订单
     *
     * 使用 REQUIRES_NEW 事务传播级别，确保：
     * 1. 此方法开始时创建新事务
     * 2. 方法返回时事务立即提交
     * 3. 已保存的 pending 订单对其他事务立即可见
     *
     * @param copyTrading 跟单配置
     * @param trade Leader 交易
     * @param tokenId 代币ID
     * @param buyQuantity 计算的买入数量
     * @param copyOrderAmount 跟单金额（USDC）
     * @param tradePrice 交易价格
     * @return PendingOrderResult 预处理结果
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun checkPositionAndSavePending(
        copyTrading: CopyTrading,
        trade: TradeResponse,
        tokenId: String,
        buyQuantity: BigDecimal,
        copyOrderAmount: BigDecimal,
        tradePrice: BigDecimal
    ): PendingOrderResult {
        // 过滤条件检查（包括仓位检查）
        // 注意：checkFilters 是 suspend 函数，需要使用 runBlocking 调用
        // 在 REQUIRES_NEW 事务中使用 runBlocking 是安全的，因为事务上下文是线程绑定的
        val filterResult = runBlocking {
            filterService.checkFilters(
                copyTrading,
                tokenId,
                tradePrice = tradePrice,
                copyOrderAmount = copyOrderAmount,
                marketId = trade.market
            )
        }

        // 如果过滤检查未通过，直接返回
        if (!filterResult.isPassed) {
            return PendingOrderResult.FilterFailed(filterResult)
        }

        var adjustedBuyQuantity = buyQuantity
        var adjustedCopyOrderAmount = copyOrderAmount

        // 如果有剩余可用仓位金额，且小于原始订单金额，则调整订单量
        val remainingPositionValue = filterResult.remainingPositionValue
        if (remainingPositionValue != null && remainingPositionValue.lt(copyOrderAmount)) {
            // 调整订单金额为剩余可用金额
            val adjustedAmount = remainingPositionValue
            // 重新计算买入数量 = 调整后金额 / 价格
            val adjustedQuantity = adjustedAmount.div(tradePrice)

            logger.info(
                "订单金额超过剩余仓位限制，调整订单量: copyTradingId=${copyTrading.id}, " +
                "原始金额=$copyOrderAmount, 剩余可用=$remainingPositionValue, " +
                "调整后金额=$adjustedAmount, 原始数量=$buyQuantity, 调整后数量=$adjustedQuantity"
            )

            adjustedBuyQuantity = adjustedQuantity
            adjustedCopyOrderAmount = adjustedAmount
        }

        // 验证订单数量限制（仅比例模式）
        var finalBuyQuantity = adjustedBuyQuantity
        if (copyTrading.copyMode == "RATIO") {
            val orderAmount = adjustedBuyQuantity.multi(trade.price.toSafeBigDecimal())
            if (orderAmount.lt(copyTrading.minOrderSize)) {
                logger.warn("订单金额低于最小限制，跳过: copyTradingId=${copyTrading.id}, amount=$orderAmount, min=${copyTrading.minOrderSize}")
                return PendingOrderResult.Skipped(
                    reason = "订单金额 $orderAmount 低于最小限制 ${copyTrading.minOrderSize}",
                    filterType = "MIN_ORDER_SIZE"
                )
            }
            if (orderAmount.gt(copyTrading.maxOrderSize)) {
                logger.warn("订单金额超过最大限制，调整数量: copyTradingId=${copyTrading.id}, amount=$orderAmount, max=${copyTrading.maxOrderSize}")
                // 调整数量到最大值
                val adjustedQty = copyTrading.maxOrderSize.div(trade.price.toSafeBigDecimal())
                if (adjustedQty.lte(BigDecimal.ZERO)) {
                    logger.warn("调整后的数量为0或负数，跳过: copyTradingId=${copyTrading.id}")
                    return PendingOrderResult.Skipped(
                        reason = "调整后的数量为0或负数",
                        filterType = "INVALID_QUANTITY"
                    )
                }
                finalBuyQuantity = adjustedQty
            }
        }

        // 计算买入价格（应用价格容忍度）
        val buyPrice = calculateAdjustedPrice(tradePrice, copyTrading, isBuy = true)

        // 检查订单簿中是否有可匹配的订单
        // 注意：getOrderbookByTokenId 是 suspend 函数，需要使用 runBlocking 调用
        val orderbookForCheck = filterResult.orderbook ?: run {
            runBlocking {
                val orderbookResult = clobService.getOrderbookByTokenId(tokenId)
                if (orderbookResult.isSuccess) orderbookResult.getOrNull() else null
            }
        }

        if (orderbookForCheck != null) {
            val bestAsk = orderbookForCheck.asks
                .mapNotNull { it.price.toSafeBigDecimal() }
                .minOrNull()

            if (bestAsk == null) {
                logger.warn("订单簿中没有卖单，跳过创建订单: copyTradingId=${copyTrading.id}, tradeId=${trade.id}")
                return PendingOrderResult.Skipped(
                    reason = "订单簿中没有卖单",
                    filterType = "NO_ASKS_IN_ORDERBOOK"
                )
            }

            if (buyPrice.lt(bestAsk)) {
                logger.warn("调整后的买入价格 ($buyPrice) 低于最佳卖单价格 ($bestAsk)，无法匹配: copyTradingId=${copyTrading.id}")
                return PendingOrderResult.Skipped(
                    reason = "买入价格 $buyPrice 低于最佳卖单价格 $bestAsk，无法匹配",
                    filterType = "PRICE_BELOW_BEST_ASK"
                )
            }
        }

        // 风险控制检查
        val riskCheckResult = checkRiskControls(copyTrading)
        if (!riskCheckResult.first) {
            logger.warn("风险控制检查失败，跳过创建订单: copyTradingId=${copyTrading.id}, reason=${riskCheckResult.second}")
            return PendingOrderResult.Skipped(
                reason = riskCheckResult.second,
                filterType = "RISK_CONTROL"
            )
        }

        // 保存 pending 状态的订单跟踪记录（预占仓位）
        // 使用临时 buyOrderId，后续 API 调用成功后会更新
        val pendingTracking = CopyOrderTracking(
            copyTradingId = copyTrading.id!!,
            accountId = copyTrading.accountId,
            leaderId = copyTrading.leaderId,
            marketId = trade.market,
            side = trade.outcomeIndex.toString(),
            outcomeIndex = trade.outcomeIndex,
            buyOrderId = "PENDING_${System.currentTimeMillis()}_${copyTrading.id}",  // 临时订单ID
            leaderBuyTradeId = trade.id,
            leaderBuyQuantity = trade.size.toSafeBigDecimal(),
            quantity = finalBuyQuantity,
            price = buyPrice,
            remainingQuantity = finalBuyQuantity,
            status = "pending",  // pending 状态，API 调用成功后更新为 filled
            notificationSent = false
        )

        val savedTracking = copyOrderTrackingRepository.save(pendingTracking)
        logger.debug("保存 pending 订单记录（独立事务，立即提交）: trackingId=${savedTracking.id}, copyTradingId=${copyTrading.id}")

        return PendingOrderResult.Success(
            trackingId = savedTracking.id!!,
            filterResult = filterResult,
            buyQuantity = finalBuyQuantity,
            buyPrice = buyPrice
        )
    }

    /**
     * 风险控制检查
     */
    private fun checkRiskControls(copyTrading: CopyTrading): Pair<Boolean, String> {
        // 1. 检查每日订单数限制
        val todayStart = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000)  // 今天0点的时间戳
        val todayBuyOrders = copyOrderTrackingRepository.findByCopyTradingId(copyTrading.id!!)
            .filter { it.createdAt >= todayStart }

        if (todayBuyOrders.size >= copyTrading.maxDailyOrders) {
            return Pair(false, "今日订单数已达上限: ${todayBuyOrders.size}/${copyTrading.maxDailyOrders}")
        }

        // 2. 检查每日亏损限制（需要计算今日已实现盈亏）
        val todaySellRecords = sellMatchRecordRepository.findByCopyTradingId(copyTrading.id)
            .filter { it.createdAt >= todayStart }

        val todayRealizedPnl = todaySellRecords.sumOf { it.totalRealizedPnl.toSafeBigDecimal() }
        if (todayRealizedPnl.lt(BigDecimal.ZERO)) {
            val todayLoss = todayRealizedPnl.abs()
            if (todayLoss.gte(copyTrading.maxDailyLoss)) {
                return Pair(false, "今日亏损已达上限: ${todayLoss}/${copyTrading.maxDailyLoss}")
            }
        }

        return Pair(true, "")
    }

    /**
     * 计算调整后的价格（应用价格容忍度）
     */
    private fun calculateAdjustedPrice(
        originalPrice: BigDecimal,
        copyTrading: CopyTrading,
        isBuy: Boolean
    ): BigDecimal {
        // 如果价格容忍度为0，使用默认值5%
        val tolerance = if (copyTrading.priceTolerance.eq(BigDecimal.ZERO)) {
            BigDecimal("5")
        } else {
            copyTrading.priceTolerance
        }

        // 计算价格调整范围（百分比）
        val tolerancePercent = tolerance.div(100)
        val adjustment = originalPrice.multi(tolerancePercent)

        return if (isBuy) {
            // 买入：可以稍微加价以确保成交（在原价格基础上加容忍度）
            originalPrice.add(adjustment).coerceAtMost(BigDecimal("0.99"))
        } else {
            // 卖出：可以稍微减价以确保成交（在原价格基础上减容忍度）
            originalPrice.subtract(adjustment).coerceAtLeast(BigDecimal("0.01"))
        }
    }
}
