-- 添加性能优化索引
-- 用于优化 OrderStatusUpdateService 的查询性能

-- copy_order_tracking 表：添加 notification_sent 索引
-- 用于 findByNotificationSentFalse() 查询
ALTER TABLE copy_order_tracking ADD INDEX idx_notification_sent (notification_sent);

-- copy_order_tracking 表：添加 created_at + notification_sent 复合索引
-- 用于 findByCreatedAtBeforeAndNotificationSentFalse() 查询
ALTER TABLE copy_order_tracking ADD INDEX idx_created_notification (created_at, notification_sent);

-- sell_match_record 表：添加 price_updated 索引
-- 用于 findByPriceUpdatedFalse() 查询
ALTER TABLE sell_match_record ADD INDEX idx_price_updated (price_updated);
