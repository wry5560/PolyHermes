package com.wrbug.polymarketbot.config

import com.wrbug.polymarketbot.dto.ApiResponse
import com.wrbug.polymarketbot.util.JwtUtils
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * JWT认证拦截器
 */
@Component
class JwtAuthenticationInterceptor(
    private val jwtUtils: JwtUtils
) : HandlerInterceptor {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationInterceptor::class.java)
    private val objectMapper = ObjectMapper()
    
    // 不需要鉴权的路径
    private val excludePaths = setOf(
        "/api/auth/login",
        "/api/auth/reset-password",
        "/api/auth/check-first-use"
    )
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val path = request.requestURI
        val method = request.method
        
        // 只拦截POST请求
        if (method != "POST") {
            return true
        }
        
        // 排除不需要鉴权的路径
        if (excludePaths.contains(path)) {
            return true
        }
        
        // 只拦截 /api/** 路径
        if (!path.startsWith("/api/")) {
            return true
        }
        
        // 从请求头获取token
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendAuthError(response, "缺少认证令牌")
            return false
        }
        
        val token = authHeader.substring(7) // 移除 "Bearer " 前缀
        
        // 验证token
        if (!jwtUtils.validateToken(token)) {
            logger.warn("Token验证失败: path=$path")
            sendAuthError(response, "认证令牌无效或已过期")
            return false
        }
        
        // 检查是否需要刷新token（使用超过1天但未过期）
        if (jwtUtils.isTokenExpiring(token)) {
            val username = jwtUtils.getUsernameFromToken(token)
            if (username != null) {
                val newToken = jwtUtils.generateToken(username)
                // 在响应头中返回新token
                response.setHeader("X-New-Token", newToken)
                logger.debug("Token自动刷新: username=$username, path=$path")
            }
        }
        
        // 将用户名存入Request属性，供后续使用
        val username = jwtUtils.getUsernameFromToken(token)
        if (username != null) {
            request.setAttribute("username", username)
        }
        
        return true
    }
    
    /**
     * 发送认证错误响应
     */
    private fun sendAuthError(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_OK
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        
        val apiResponse: ApiResponse<Unit> = ApiResponse.authError(message)
        val json = objectMapper.writeValueAsString(apiResponse)
        response.writer.write(json)
        response.writer.flush()
    }
}

