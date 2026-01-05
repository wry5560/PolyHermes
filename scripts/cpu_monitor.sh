#!/bin/bash
#
# CPU 监控告警脚本
# 当 CPU 使用率超过阈值时通过 Telegram 发送告警
#
# 用法: ./cpu_monitor.sh
# 建议通过 cron 每分钟运行一次:
# * * * * * /opt/polyhermes/scripts/cpu_monitor.sh >> /var/log/cpu_monitor.log 2>&1
#

# 配置
CPU_THRESHOLD=70              # CPU 使用率阈值（百分比）
ALERT_COOLDOWN=300            # 告警冷却时间（秒），避免重复告警
COOLDOWN_FILE="/tmp/cpu_alert_cooldown"

# Telegram 配置（从环境变量或 .env 文件读取）
if [ -f /opt/polyhermes/.env ]; then
    source /opt/polyhermes/.env 2>/dev/null
fi

# 获取当前 CPU 使用率（取最近1分钟平均）
get_cpu_usage() {
    # 使用 top 获取 CPU 空闲率，然后计算使用率
    local idle=$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/")
    local usage=$(echo "100 - $idle" | bc)
    echo "${usage%.*}"
}

# 检查冷却时间
check_cooldown() {
    if [ -f "$COOLDOWN_FILE" ]; then
        local last_alert=$(cat "$COOLDOWN_FILE")
        local now=$(date +%s)
        local diff=$((now - last_alert))
        if [ $diff -lt $ALERT_COOLDOWN ]; then
            return 1  # 仍在冷却中
        fi
    fi
    return 0  # 可以发送告警
}

# 发送 Telegram 告警
send_telegram_alert() {
    local cpu_usage=$1
    local message="⚠️ *CPU 使用率告警*

服务器: $(hostname)
CPU 使用率: *${cpu_usage}%*
阈值: ${CPU_THRESHOLD}%
时间: $(date '+%Y-%m-%d %H:%M:%S')

请检查服务器状态！"

    # 如果配置了 Telegram，发送告警
    if [ -n "$TELEGRAM_BOT_TOKEN" ] && [ -n "$TELEGRAM_CHAT_ID" ]; then
        curl -s -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
            -d "chat_id=${TELEGRAM_CHAT_ID}" \
            -d "text=${message}" \
            -d "parse_mode=Markdown" > /dev/null 2>&1
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] 已发送 Telegram 告警: CPU=${cpu_usage}%"
    else
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] 警告: Telegram 未配置，无法发送告警"
    fi

    # 记录告警时间
    date +%s > "$COOLDOWN_FILE"
}

# 主逻辑
main() {
    local cpu_usage=$(get_cpu_usage)

    if [ "$cpu_usage" -ge "$CPU_THRESHOLD" ]; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] CPU 使用率 ${cpu_usage}% 超过阈值 ${CPU_THRESHOLD}%"

        if check_cooldown; then
            send_telegram_alert "$cpu_usage"
        else
            echo "[$(date '+%Y-%m-%d %H:%M:%S')] 在冷却期内，跳过告警"
        fi
    fi
}

main
