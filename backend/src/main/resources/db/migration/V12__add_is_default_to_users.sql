-- 添加 is_default 字段到 users 表
ALTER TABLE users 
ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否默认账户（首次创建的用户）';

-- 为默认账户添加索引
ALTER TABLE users 
ADD INDEX idx_is_default (is_default);

