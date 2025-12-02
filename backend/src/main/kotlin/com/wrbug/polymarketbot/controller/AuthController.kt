package com.wrbug.polymarketbot.controller

import com.wrbug.polymarketbot.dto.*
import com.wrbug.polymarketbot.enums.ErrorCode
import com.wrbug.polymarketbot.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    /**
     * 登录接口
     */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        return try {
            if (request.username.isBlank()) {
                return ResponseEntity.ok(ApiResponse.paramError("用户名不能为空"))
            }
            if (request.password.isBlank()) {
                return ResponseEntity.ok(ApiResponse.paramError("密码不能为空"))
            }
            
            val result = authService.login(request.username, request.password)
            result.fold(
                onSuccess = { loginResponse ->
                    ResponseEntity.ok(ApiResponse.success(loginResponse))
                },
                onFailure = { e ->
                    logger.error("登录失败: ${e.message}", e)
                    when (e) {
                        is IllegalArgumentException -> {
                            if (e.message == ErrorCode.AUTH_USERNAME_OR_PASSWORD_ERROR.message) {
                                ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_USERNAME_OR_PASSWORD_ERROR.code, e.message ?: "用户名或密码错误"))
                            } else {
                                ResponseEntity.ok(ApiResponse.paramError(e.message ?: "参数错误"))
                            }
                        }
                        else -> ResponseEntity.ok(ApiResponse.serverError("登录失败: ${e.message}"))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("登录异常: ${e.message}", e)
            ResponseEntity.ok(ApiResponse.serverError("登录失败: ${e.message}"))
        }
    }
    
    /**
     * 重置密码接口
     */
    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestBody request: ResetPasswordRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            if (request.resetKey.isBlank()) {
                return ResponseEntity.ok(ApiResponse.paramError("重置密钥不能为空"))
            }
            if (request.username.isBlank()) {
                return ResponseEntity.ok(ApiResponse.paramError("用户名不能为空"))
            }
            if (request.newPassword.isBlank()) {
                return ResponseEntity.ok(ApiResponse.paramError("新密码不能为空"))
            }
            
            val result = authService.resetPassword(
                resetKey = request.resetKey,
                username = request.username,
                newPassword = request.newPassword,
                request = httpRequest
            )
            
            result.fold(
                onSuccess = {
                    ResponseEntity.ok(ApiResponse.success(Unit))
                },
                onFailure = { e ->
                    logger.error("重置密码失败: ${e.message}", e)
                    // 统一返回"重置失败"，不暴露具体错误原因（安全考虑）
                    // 但密码强度错误可以提示，因为这是输入格式问题
                    when (e) {
                        is IllegalArgumentException -> {
                            if (e.message == ErrorCode.AUTH_PASSWORD_WEAK.message) {
                                // 密码强度错误可以提示
                                ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_PASSWORD_WEAK.code, e.message ?: "密码长度不符合要求"))
                            } else {
                                // 其他错误统一返回"重置失败"
                                ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_ERROR.code, "重置失败"))
                            }
                        }
                        is IllegalStateException -> {
                            // 频率限制等错误统一返回"重置失败"
                            ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_ERROR.code, "重置失败"))
                        }
                        else -> {
                            ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_ERROR.code, "重置失败"))
                        }
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("重置密码异常: ${e.message}", e)
            ResponseEntity.ok(ApiResponse.serverError("重置密码失败: ${e.message}"))
        }
    }
    
    /**
     * 检查是否首次使用接口
     */
    @PostMapping("/check-first-use")
    fun checkFirstUse(): ResponseEntity<ApiResponse<CheckFirstUseResponse>> {
        return try {
            val isFirstUse = authService.isFirstUse()
            ResponseEntity.ok(ApiResponse.success(CheckFirstUseResponse(isFirstUse = isFirstUse)))
        } catch (e: Exception) {
            logger.error("检查首次使用异常: ${e.message}", e)
            ResponseEntity.ok(ApiResponse.serverError("检查首次使用失败: ${e.message}"))
        }
    }
}

