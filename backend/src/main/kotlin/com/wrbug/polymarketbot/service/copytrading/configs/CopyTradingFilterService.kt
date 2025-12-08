package com.wrbug.polymarketbot.service.copytrading.configs

import com.wrbug.polymarketbot.api.OrderbookResponse
import com.wrbug.polymarketbot.entity.CopyTrading
import com.wrbug.polymarketbot.util.gt
import com.wrbug.polymarketbot.util.lt
import com.wrbug.polymarketbot.util.multi
import com.wrbug.polymarketbot.util.toSafeBigDecimal
import org.slf4j.LoggerFactory
import com.wrbug.polymarketbot.service.common.PolymarketClobService
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * 跟单过滤条件检查服务
 */
@Service
class CopyTradingFilterService(
    private val clobService: PolymarketClobService
) {
    
    private val logger = LoggerFactory.getLogger(CopyTradingFilterService::class.java)
    
    /**
     * 检查过滤条件
     * @param copyTrading 跟单配置
     * @param tokenId token ID（用于获取订单簿）
     * @param isBuyOrder 是否为买入订单（true=买入，false=卖出）
     * @return Pair<是否通过, 失败原因>
     */
    suspend fun checkFilters(
        copyTrading: CopyTrading,
        tokenId: String,
        isBuyOrder: Boolean,
        tradePrice: BigDecimal? = null  // Leader 交易价格，用于价格区间检查
    ): Pair<Boolean, String> {
        // 1. 价格区间检查（如果配置了价格区间）
        if (tradePrice != null) {
            val priceRangeCheck = checkPriceRange(copyTrading, tradePrice)
            if (!priceRangeCheck.first) {
                return priceRangeCheck
            }
        }
        
        // 2. 价格合理性检查（基础检查，无需配置）
        // 这个检查在获取订单簿时进行，如果价格不在 0.01-0.99 范围内，订单簿获取会失败
        
        // 3. 获取订单簿
        val orderbookResult = clobService.getOrderbookByTokenId(tokenId)
        if (!orderbookResult.isSuccess) {
            val error = orderbookResult.exceptionOrNull()
            return Pair(false, "获取订单簿失败: ${error?.message ?: "未知错误"}")
        }
        
        val orderbook = orderbookResult.getOrNull()
        if (orderbook == null) {
            return Pair(false, "订单簿为空")
        }
        
        // 4. 买一卖一价差过滤
        val spreadCheck = checkSpread(copyTrading, orderbook)
        if (!spreadCheck.first) {
            return spreadCheck
        }
        
        // 5. 订单深度过滤
        val depthCheck = checkOrderDepth(copyTrading, orderbook, isBuyOrder)
        if (!depthCheck.first) {
            return depthCheck
        }
        
        // 6. 最小订单簿深度过滤（可选）
        val orderbookDepthCheck = checkOrderbookDepth(copyTrading, orderbook, isBuyOrder)
        if (!orderbookDepthCheck.first) {
            return orderbookDepthCheck
        }
        
        return Pair(true, "")
    }
    
    /**
     * 检查价格区间
     * @param copyTrading 跟单配置
     * @param tradePrice Leader 交易价格
     * @return Pair<是否通过, 失败原因>
     */
    private fun checkPriceRange(
        copyTrading: CopyTrading,
        tradePrice: BigDecimal
    ): Pair<Boolean, String> {
        // 如果未配置价格区间，直接通过
        if (copyTrading.minPrice == null && copyTrading.maxPrice == null) {
            return Pair(true, "")
        }
        
        // 检查最低价格
        if (copyTrading.minPrice != null && tradePrice.lt(copyTrading.minPrice)) {
            return Pair(false, "价格低于最低限制: $tradePrice < ${copyTrading.minPrice}")
        }
        
        // 检查最高价格
        if (copyTrading.maxPrice != null && tradePrice.gt(copyTrading.maxPrice)) {
            return Pair(false, "价格高于最高限制: $tradePrice > ${copyTrading.maxPrice}")
        }
        
        return Pair(true, "")
    }
    
    /**
     * 检查买一卖一价差
     * bestBid: 买盘中的最高价格（最大值）
     * bestAsk: 卖盘中的最低价格（最小值）
     */
    private fun checkSpread(
        copyTrading: CopyTrading,
        orderbook: OrderbookResponse
    ): Pair<Boolean, String> {
        // 如果未启用价差过滤，直接通过
        if (copyTrading.maxSpread == null) {
            return Pair(true, "")
        }
        
        // 获取买盘中的最高价格（bestBid = bids 中的最大值）
        val bestBid = orderbook.bids
            .mapNotNull { it.price.toSafeBigDecimal() }
            .maxOrNull()
        
        // 获取卖盘中的最低价格（bestAsk = asks 中的最小值）
        val bestAsk = orderbook.asks
            .mapNotNull { it.price.toSafeBigDecimal() }
            .minOrNull()
        
        if (bestBid == null || bestAsk == null) {
            return Pair(false, "订单簿缺少买一或卖一价格")
        }
        
        // 计算价差（绝对价格）
        val spread = bestAsk.subtract(bestBid)
        
        if (spread.gt(copyTrading.maxSpread)) {
            return Pair(false, "价差过大: $spread > ${copyTrading.maxSpread}")
        }
        
        return Pair(true, "")
    }
    
    /**
     * 检查订单深度
     */
    private fun checkOrderDepth(
        copyTrading: CopyTrading,
        orderbook: OrderbookResponse,
        isBuyOrder: Boolean
    ): Pair<Boolean, String> {
        // 如果未启用订单深度过滤，直接通过
        if (copyTrading.minOrderDepth == null) {
            return Pair(true, "")
        }
        
        // 对于买入订单，检查卖盘（asks）深度
        // 对于卖出订单，检查买盘（bids）深度
        val orders = if (isBuyOrder) orderbook.asks else orderbook.bids
        
        // 计算总深度（累计订单金额）
        var totalDepth = BigDecimal.ZERO
        for (order in orders) {
            val price = order.price.toSafeBigDecimal()
            val size = order.size.toSafeBigDecimal()
            val orderAmount = price.multi(size)
            totalDepth = totalDepth.add(orderAmount)
        }
        
        if (totalDepth.lt(copyTrading.minOrderDepth)) {
            return Pair(false, "订单深度不足: $totalDepth < ${copyTrading.minOrderDepth}")
        }
        
        return Pair(true, "")
    }
    
    /**
     * 检查最小订单簿深度（前 N 档深度）
     */
    private fun checkOrderbookDepth(
        copyTrading: CopyTrading,
        orderbook: OrderbookResponse,
        isBuyOrder: Boolean
    ): Pair<Boolean, String> {
        // 如果未启用最小订单簿深度过滤，直接通过
        if (copyTrading.minOrderbookDepth == null) {
            return Pair(true, "")
        }
        
        // 对于买入订单，检查卖盘（asks）前 3 档深度
        // 对于卖出订单，检查买盘（bids）前 3 档深度
        val orders = if (isBuyOrder) orderbook.asks else orderbook.bids
        val topNOrders = orders.take(3)  // 前 3 档
        
        // 计算前 N 档总深度
        var totalDepth = BigDecimal.ZERO
        for (order in topNOrders) {
            val price = order.price.toSafeBigDecimal()
            val size = order.size.toSafeBigDecimal()
            val orderAmount = price.multi(size)
            totalDepth = totalDepth.add(orderAmount)
        }
        
        if (totalDepth.lt(copyTrading.minOrderbookDepth)) {
            return Pair(false, "订单簿深度不足: $totalDepth < ${copyTrading.minOrderbookDepth}")
        }
        
        return Pair(true, "")
    }
}

