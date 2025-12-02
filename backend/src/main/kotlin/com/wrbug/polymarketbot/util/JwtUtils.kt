package com.wrbug.polymarketbot.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT工具类
 */
@Component
class JwtUtils {
    
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    
    @Value("\${jwt.expiration}")
    private var expiration: Long = 604800000  // 7天，默认值
    
    @Value("\${jwt.refresh-threshold}")
    private var refreshThreshold: Long = 86400000  // 1天，默认值
    
    /**
     * 获取签名密钥
     */
    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(secret.toByteArray())
    }
    
    /**
     * 生成JWT token
     * @param username 用户名
     * @return JWT token字符串
     */
    fun generateToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)
        
        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }
    
    /**
     * 从token中获取Claims
     */
    private fun getClaimsFromToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 验证token是否有效
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            claims != null && !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 从token中获取用户名
     */
    fun getUsernameFromToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims?.subject
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取token签发时间（毫秒时间戳）
     */
    fun getIssuedAtFromToken(token: String): Long? {
        return try {
            val claims = getClaimsFromToken(token)
            claims?.issuedAt?.time
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 判断token是否已过期
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            val expiration = claims?.expiration ?: return true
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }
    
    /**
     * 判断token是否使用超过1天但未过期（用于自动刷新）
     */
    fun isTokenExpiring(token: String): Boolean {
        return try {
            if (isTokenExpired(token)) {
                return false
            }
            val issuedAt = getIssuedAtFromToken(token) ?: return false
            val now = System.currentTimeMillis()
            val timeSinceIssued = now - issuedAt
            // 如果使用时间超过刷新阈值（1天）但未过期，则需要刷新
            timeSinceIssued >= refreshThreshold
        } catch (e: Exception) {
            false
        }
    }
}

