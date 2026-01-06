package com.wrbug.polymarketbot.repository

import com.wrbug.polymarketbot.entity.CopyOrderTracking
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

/**
 * 订单跟踪Repository
 */
@Repository
interface CopyOrderTrackingRepository : JpaRepository<CopyOrderTracking, Long> {
    
    /**
     * 根据跟单关系ID查询所有买入订单
     */
    fun findByCopyTradingId(copyTradingId: Long): List<CopyOrderTracking>
    
    /**
     * 根据跟单关系ID、市场ID和方向查询未匹配的买入订单（FIFO顺序）
     * @deprecated 使用 findUnmatchedBuyOrdersByOutcomeIndex 替代
     */
    @Query("SELECT t FROM CopyOrderTracking t WHERE t.copyTradingId = :copyTradingId AND t.marketId = :marketId AND t.side = :side AND t.remainingQuantity > 0 ORDER BY t.createdAt ASC")
    fun findUnmatchedBuyOrders(copyTradingId: Long, marketId: String, side: String): List<CopyOrderTracking>
    
    /**
     * 根据跟单关系ID、市场ID和outcomeIndex查询未匹配的买入订单（FIFO顺序）
     * 支持多元市场（不限于YES/NO）
     */
    @Query("SELECT t FROM CopyOrderTracking t WHERE t.copyTradingId = :copyTradingId AND t.marketId = :marketId AND t.outcomeIndex = :outcomeIndex AND t.remainingQuantity > 0 ORDER BY t.createdAt ASC")
    fun findUnmatchedBuyOrdersByOutcomeIndex(copyTradingId: Long, marketId: String, outcomeIndex: Int): List<CopyOrderTracking>
    
    /**
     * 根据跟单关系ID和状态查询订单
     */
    fun findByCopyTradingIdAndStatus(copyTradingId: Long, status: String): List<CopyOrderTracking>
    
    /**
     * 根据跟单关系ID和市场ID查询订单
     */
    fun findByCopyTradingIdAndMarketId(copyTradingId: Long, marketId: String): List<CopyOrderTracking>
    
    /**
     * 根据Leader交易ID查询订单
     */
    fun findByLeaderBuyTradeId(leaderBuyTradeId: String): CopyOrderTracking?
    
    /**
     * 根据买入订单ID查询订单跟踪记录
     */
    fun findByBuyOrderId(buyOrderId: String): List<CopyOrderTracking>
    
    /**
     * 查询未发送通知的买入订单（用于轮询更新）
     */
    fun findByNotificationSentFalse(): List<CopyOrderTracking>
    
    /**
     * 查询指定时间之前创建的订单（用于检查30秒后未成交的订单）
     */
    @Query("SELECT t FROM CopyOrderTracking t WHERE t.createdAt <= :beforeTime")
    fun findByCreatedAtBefore(beforeTime: Long): List<CopyOrderTracking>

    /**
     * 查询指定时间之前创建且未发送通知的订单
     * 只检查未处理的订单，避免重复查询已确认成交的订单
     */
    @Query("SELECT t FROM CopyOrderTracking t WHERE t.createdAt <= :beforeTime AND t.notificationSent = false")
    fun findByCreatedAtBeforeAndNotificationSentFalse(beforeTime: Long): List<CopyOrderTracking>

    /**
     * 计算某个跟单配置在某个市场的活跃仓位总金额（用于最大仓位限制检查）
     * 只统计未完全卖出的订单（状态不为 fully_matched）
     * 使用 remainingQuantity（剩余未卖出数量）× price（买入价格）计算
     * 注意：使用 remainingQuantity 而不是 quantity，确保部分卖出后仓位值正确计算
     */
    @Query("""
        SELECT COALESCE(SUM(t.remainingQuantity * t.price), 0)
        FROM CopyOrderTracking t
        WHERE t.copyTradingId = :copyTradingId
        AND t.marketId = :marketId
        AND t.status != 'fully_matched'
    """)
    fun sumActivePositionValue(copyTradingId: Long, marketId: String): BigDecimal

    /**
     * 统计某个跟单配置在某个市场的活跃仓位数量（用于最大仓位数量检查）
     * 只统计未完全卖出的订单（状态不为 fully_matched）
     */
    @Query("""
        SELECT COUNT(t)
        FROM CopyOrderTracking t
        WHERE t.copyTradingId = :copyTradingId
        AND t.marketId = :marketId
        AND t.status != 'fully_matched'
    """)
    fun countActivePositions(copyTradingId: Long, marketId: String): Long
}

