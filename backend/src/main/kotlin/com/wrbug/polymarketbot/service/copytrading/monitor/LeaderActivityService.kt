package com.wrbug.polymarketbot.service.copytrading.monitor

import com.wrbug.polymarketbot.api.UserActivityResponse
import com.wrbug.polymarketbot.entity.LeaderActivityRecord
import com.wrbug.polymarketbot.repository.LeaderActivityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Leader 活动记录服务
 * 用于记录 Leader 的所有链上活动（包括 TRADE, SPLIT, MERGE, REDEEM 等）
 */
@Service
class LeaderActivityService(
    private val leaderActivityRepository: LeaderActivityRepository
) {
    private val logger = LoggerFactory.getLogger(LeaderActivityService::class.java)

    /**
     * 记录 Leader 活动
     *
     * @param leaderId Leader ID
     * @param activity 活动数据
     * @param source 数据来源（polling）
     * @return 是否成功记录（如果已存在则返回 false）
     */
    @Transactional
    fun recordActivity(leaderId: Long, activity: UserActivityResponse, source: String): Boolean {
        val transactionHash = activity.transactionHash

        // 检查是否已记录
        if (transactionHash != null && leaderActivityRepository.existsByLeaderIdAndTransactionHash(leaderId, transactionHash)) {
            return false
        }

        try {
            val record = LeaderActivityRecord(
                leaderId = leaderId,
                transactionHash = transactionHash,
                activityType = activity.type,
                tradeSide = activity.side,
                marketId = activity.conditionId,
                marketTitle = activity.title,
                outcomeIndex = activity.outcomeIndex,
                outcomeName = activity.outcome,
                size = activity.size?.let { BigDecimal.valueOf(it) },
                price = activity.price?.let { BigDecimal.valueOf(it) },
                usdcSize = activity.usdcSize?.let { BigDecimal.valueOf(it) },
                source = source,
                timestamp = activity.timestamp * 1000,  // 转换为毫秒
                createdAt = System.currentTimeMillis()
            )

            leaderActivityRepository.save(record)

            // 仅对非 TRADE 类型记录日志（TRADE 类型由跟单服务处理）
            if (activity.type != "TRADE") {
                logger.info(
                    "记录 Leader 活动: leaderId={}, type={}, market={}, size={}, usdcSize={}",
                    leaderId, activity.type, activity.title, activity.size, activity.usdcSize
                )
            }

            return true
        } catch (e: Exception) {
            // 可能是唯一约束冲突，忽略
            logger.debug("记录活动失败（可能已存在）: leaderId={}, txHash={}, error={}", leaderId, transactionHash, e.message)
            return false
        }
    }

    /**
     * 批量记录 Leader 活动
     */
    @Transactional
    fun recordActivities(leaderId: Long, activities: List<UserActivityResponse>, source: String): Int {
        var count = 0
        activities.forEach { activity ->
            if (recordActivity(leaderId, activity, source)) {
                count++
            }
        }
        return count
    }

    /**
     * 检查活动是否已记录
     */
    fun isActivityRecorded(leaderId: Long, transactionHash: String): Boolean {
        return leaderActivityRepository.existsByLeaderIdAndTransactionHash(leaderId, transactionHash)
    }
}
