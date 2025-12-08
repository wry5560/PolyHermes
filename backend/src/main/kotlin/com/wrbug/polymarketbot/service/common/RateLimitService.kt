package com.wrbug.polymarketbot.service.common

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference

/**
 * 频率限制服务（使用内存缓存，全局限制）
 */
@Service
class RateLimitService {
    
    private val logger = LoggerFactory.getLogger(RateLimitService::class.java)
    
    @Value("\${rate-limit.reset-password.max-attempts:3}")
    private var maxAttempts: Int = 3
    
    @Value("\${rate-limit.reset-password.window-seconds:60}")
    private var windowSeconds: Long = 60
    
    // 全局尝试记录列表（时间戳），所有请求共享
    private val resetPasswordAttempts = AtomicReference<MutableList<Long>>(mutableListOf())
    
    /**
     * 检查重置密码频率限制（全局限制，不按IP）
     * @return Result，如果超过限制则返回失败
     */
    fun checkResetPasswordRateLimit(): Result<Unit> {
        val now = System.currentTimeMillis()
        val windowStart = now - (windowSeconds * 1000)
        
        // 获取当前尝试记录列表
        val attempts = resetPasswordAttempts.get()
        
        // 清理过期记录（超过时间窗口的记录）
        val validAttempts = attempts.filter { it >= windowStart }.toMutableList()
        
        // 检查是否超过限制
        if (validAttempts.size >= maxAttempts) {
            logger.warn("重置密码频率限制触发: attempts=${validAttempts.size}/$maxAttempts")
            return Result.failure(IllegalStateException("频率限制：1分钟内最多尝试${maxAttempts}次，请稍后再试"))
        }
        
        // 记录本次尝试
        validAttempts.add(now)
        resetPasswordAttempts.set(validAttempts)
        
        return Result.success(Unit)
    }
}

