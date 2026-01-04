-- Add notification_config_id to copy_trading table
-- NULL means send to all enabled notification configs (backward compatible)
ALTER TABLE copy_trading ADD COLUMN notification_config_id BIGINT NULL;

-- Add foreign key constraint
ALTER TABLE copy_trading ADD CONSTRAINT fk_copy_trading_notification_config
    FOREIGN KEY (notification_config_id) REFERENCES notification_configs(id) ON DELETE SET NULL;

-- Add index for faster lookups
CREATE INDEX idx_copy_trading_notification_config_id ON copy_trading(notification_config_id);
