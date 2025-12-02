-- ============================================
-- 创建代理配置表
-- ============================================
CREATE TABLE IF NOT EXISTS proxy_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL COMMENT '代理类型：HTTP, CLASH, SS',
    enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否启用',
    host VARCHAR(255) NULL COMMENT '代理主机（HTTP代理）',
    port INT NULL COMMENT '代理端口（HTTP代理）',
    username VARCHAR(100) NULL COMMENT '代理用户名（HTTP代理，可选）',
    password VARCHAR(255) NULL COMMENT '代理密码（HTTP代理，可选，BCrypt加密）',
    subscription_url VARCHAR(500) NULL COMMENT '订阅链接（Clash/SS代理）',
    subscription_config TEXT NULL COMMENT '订阅配置内容（Clash/SS代理，JSON格式）',
    last_subscription_update BIGINT NULL COMMENT '最后订阅更新时间（毫秒时间戳）',
    created_at BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
    updated_at BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
    INDEX idx_type (type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理配置表';

