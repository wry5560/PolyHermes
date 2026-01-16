package com.wrbug.polymarketbot.repository

import com.wrbug.polymarketbot.entity.LeaderActivityRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Leader 活动记录 Repository
 */
@Repository
interface LeaderActivityRepository : JpaRepository<LeaderActivityRecord, Long> {

    /**
     * 检查活动是否已记录
     */
    fun existsByLeaderIdAndTransactionHash(leaderId: Long, transactionHash: String): Boolean

    /**
     * 根据 Leader ID 查询活动记录（分页）
     */
    fun findByLeaderIdOrderByTimestampDesc(leaderId: Long, pageable: Pageable): Page<LeaderActivityRecord>

    /**
     * 根据 Leader ID 和活动类型查询活动记录（分页）
     */
    fun findByLeaderIdAndActivityTypeOrderByTimestampDesc(
        leaderId: Long,
        activityType: String,
        pageable: Pageable
    ): Page<LeaderActivityRecord>

    /**
     * 根据 Leader ID 查询指定类型的活动记录
     */
    fun findByLeaderIdAndActivityTypeInOrderByTimestampDesc(
        leaderId: Long,
        activityTypes: List<String>,
        pageable: Pageable
    ): Page<LeaderActivityRecord>

    /**
     * 统计 Leader 的活动数量（按类型）
     */
    @Query("SELECT r.activityType, COUNT(r) FROM LeaderActivityRecord r WHERE r.leaderId = :leaderId GROUP BY r.activityType")
    fun countByLeaderIdGroupByActivityType(leaderId: Long): List<Array<Any>>

    /**
     * 删除过期记录
     */
    @Modifying
    @Query("DELETE FROM LeaderActivityRecord r WHERE r.createdAt < :expireTime")
    fun deleteByCreatedAtBefore(expireTime: Long): Int
}
