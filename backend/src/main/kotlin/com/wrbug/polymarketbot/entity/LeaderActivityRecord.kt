package com.wrbug.polymarketbot.entity

import jakarta.persistence.*
import java.math.BigDecimal

/**
 * Leader 活动记录实体
 * 用于记录 Leader 的所有链上活动（TRADE, SPLIT, MERGE, REDEEM 等）
 */
@Entity
@Table(
    name = "leader_activity_record",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["leader_id", "transaction_hash"])
    ]
)
data class LeaderActivityRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "leader_id", nullable = false)
    val leaderId: Long,

    @Column(name = "transaction_hash", length = 100)
    val transactionHash: String?,

    @Column(name = "activity_type", nullable = false, length = 20)
    val activityType: String,  // TRADE, SPLIT, MERGE, REDEEM, REWARD, CONVERSION

    @Column(name = "trade_side", length = 10)
    val tradeSide: String?,  // BUY/SELL（仅 TRADE 类型有此字段）

    @Column(name = "market_id", nullable = false, length = 100)
    val marketId: String,

    @Column(name = "market_title", length = 500)
    val marketTitle: String?,

    @Column(name = "outcome_index")
    val outcomeIndex: Int?,

    @Column(name = "outcome_name", length = 100)
    val outcomeName: String?,

    @Column(name = "size", precision = 20, scale = 8)
    val size: BigDecimal?,

    @Column(name = "price", precision = 20, scale = 8)
    val price: BigDecimal?,

    @Column(name = "usdc_size", precision = 20, scale = 8)
    val usdcSize: BigDecimal?,

    @Column(name = "source", nullable = false, length = 20)
    val source: String,  // polling

    @Column(name = "timestamp", nullable = false)
    val timestamp: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Long = System.currentTimeMillis()
)
