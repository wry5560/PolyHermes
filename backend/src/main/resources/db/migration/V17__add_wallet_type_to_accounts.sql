-- 添加钱包类型字段到 wallet_accounts 表
-- magic: 邮箱/OAuth 登录用户（使用 CREATE2 计算代理地址）
-- safe: MetaMask 等外部钱包用户（使用 Safe 代理合约）

ALTER TABLE wallet_accounts
ADD COLUMN wallet_type VARCHAR(20) NOT NULL DEFAULT 'magic' COMMENT '钱包类型: magic 或 safe';

-- 为现有账户设置默认值
-- 注意：如果用户使用的是 MetaMask 登录，需要手动更新为 safe
UPDATE wallet_accounts SET wallet_type = 'magic' WHERE wallet_type IS NULL OR wallet_type = '';
