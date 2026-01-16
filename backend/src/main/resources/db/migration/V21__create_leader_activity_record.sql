-- ============================================
-- V21: 创建 Leader 活动记录表
-- 用于记录 Leader 的所有链上活动（TRADE, SPLIT, MERGE, REDEEM 等）
-- ============================================

CREATE TABLE IF NOT EXISTS leader_activity_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    leader_id BIGINT NOT NULL COMMENT 'Leader ID',
    transaction_hash VARCHAR(100) COMMENT '交易哈希',
    activity_type VARCHAR(20) NOT NULL COMMENT '活动类型：TRADE, SPLIT, MERGE, REDEEM, REWARD, CONVERSION',
    trade_side VARCHAR(10) COMMENT 'BUY/SELL（仅 TRADE 类型有此字段）',
    market_id VARCHAR(100) NOT NULL COMMENT '市场 ID (conditionId)',
    market_title VARCHAR(500) COMMENT '市场标题',
    outcome_index INT COMMENT '结果索引（0, 1, 2, ...）',
    outcome_name VARCHAR(100) COMMENT '结果名称（如 YES, NO）',
    size DECIMAL(20, 8) COMMENT '数量',
    price DECIMAL(20, 8) COMMENT '价格（仅 TRADE 类型有此字段）',
    usdc_size DECIMAL(20, 8) COMMENT 'USDC 金额',
    source VARCHAR(20) NOT NULL COMMENT '数据来源：polling',
    timestamp BIGINT NOT NULL COMMENT '活动时间（毫秒时间戳）',
    created_at BIGINT NOT NULL COMMENT '记录创建时间（毫秒时间戳）',

    -- 唯一约束：防止重复记录同一活动
    UNIQUE KEY uk_leader_activity (leader_id, transaction_hash),

    -- 索引优化查询性能
    INDEX idx_leader_id (leader_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_market_id (market_id),

    FOREIGN KEY (leader_id) REFERENCES copy_trading_leaders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Leader 活动记录表（用于监控所有链上活动）';
