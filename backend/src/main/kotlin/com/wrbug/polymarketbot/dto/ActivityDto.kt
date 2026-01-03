package com.wrbug.polymarketbot.dto

/**
 * 账户活动 DTO（交易历史）
 */
data class AccountActivityDto(
    val accountId: Long,
    val accountName: String?,
    val walletAddress: String,
    val proxyAddress: String,
    val timestamp: Long,           // Unix 时间戳（秒）
    val type: String,              // TRADE, REDEEM, SPLIT, MERGE, REWARD, CONVERSION
    val side: String?,             // BUY, SELL (仅 TRADE 类型有)
    val marketId: String,          // conditionId
    val marketTitle: String?,
    val marketSlug: String?,
    val marketIcon: String?,
    val outcome: String?,          // 选择的结果（YES/NO 或具体名称）
    val outcomeIndex: Int?,
    val size: String?,             // 数量
    val price: String?,            // 价格
    val usdcSize: String?,         // USDC 金额
    val transactionHash: String?   // 交易哈希
)

/**
 * 活动列表响应
 */
data class ActivityListResponse(
    val activities: List<AccountActivityDto>,
    val total: Int
)

/**
 * 活动列表请求
 */
data class ActivityListRequest(
    val accountId: Long? = null,   // 可选：按账户筛选
    val type: String? = null,      // 可选：按类型筛选 (TRADE, REDEEM, etc.)
    val limit: Int = 100,          // 每页数量
    val offset: Int = 0            // 偏移量
)
