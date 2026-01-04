-- 为跟单模板添加仓位配置和通知配置字段
-- 与 CopyTrading 实体保持一致

-- 添加最大仓位金额字段
ALTER TABLE copy_trading_templates ADD COLUMN max_position_value DECIMAL(20, 8) NULL COMMENT '最大仓位金额（USDC），NULL表示不启用';

-- 添加最大仓位数量字段
ALTER TABLE copy_trading_templates ADD COLUMN max_position_count INT NULL COMMENT '最大仓位数量，NULL表示不启用';

-- 添加推送失败订单字段
ALTER TABLE copy_trading_templates ADD COLUMN push_failed_orders BOOLEAN NOT NULL DEFAULT FALSE COMMENT '推送失败订单（默认关闭）';
