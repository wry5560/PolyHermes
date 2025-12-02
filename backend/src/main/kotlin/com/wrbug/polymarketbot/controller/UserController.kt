package com.wrbug.polymarketbot.controller

import com.wrbug.polymarketbot.dto.*
import com.wrbug.polymarketbot.enums.ErrorCode
import com.wrbug.polymarketbot.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    /**
     * 获取当前用户名（从Request属性中获取）
     */
    private fun getCurrentUsername(request: HttpServletRequest): String? {
        return request.getAttribute("username") as? String
    }
    
    /**
     * 验证是否为默认账户
     */
    private fun checkDefaultUser(request: HttpServletRequest): String? {
        val username = getCurrentUsername(request)
        if (username == null) {
            return "未获取到用户信息"
        }
        if (!userService.isDefaultUser(username)) {
            return "只有默认账户可以执行此操作"
        }
        return null
    }
    
    /**
     * 获取用户列表
     * 所有用户都可以访问，但非默认账户只能看到自己的信息
     */
    @PostMapping("/list")
    fun getUserList(request: HttpServletRequest): ResponseEntity<ApiResponse<List<UserDto>>> {
        return try {
            val currentUsername = getCurrentUsername(request)
            if (currentUsername == null) {
                return ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_ERROR.code, "未获取到用户信息"))
            }
            
            val users = userService.getUserList(currentUsername)
            ResponseEntity.ok(ApiResponse.success(users))
        } catch (e: Exception) {
            logger.error("获取用户列表异常: ${e.message}", e)
            ResponseEntity.ok(ApiResponse.serverError("获取用户列表失败: ${e.message}"))
        }
    }
    
    /**
     * 创建用户
     */
    @PostMapping("/create")
    fun createUser(
        @RequestBody requestBody: UserCreateRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val error = checkDefaultUser(httpRequest)
            if (error != null) {
                return ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_PERMISSION_DENIED.code, error))
            }
            
            val currentUsername = getCurrentUsername(httpRequest)!!
            val result = userService.createUser(requestBody, currentUsername)
            
            result.fold(
                onSuccess = { user ->
                    ResponseEntity.ok(ApiResponse.success(user))
                },
                onFailure = { e ->
                    logger.error("创建用户失败: ${e.message}", e)
                    when (e) {
                        is IllegalArgumentException -> ResponseEntity.ok(ApiResponse.paramError(e.message ?: "参数错误"))
                        is IllegalStateException -> ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_PERMISSION_DENIED.code, e.message ?: "权限不足"))
                        else -> ResponseEntity.ok(ApiResponse.serverError("创建用户失败: ${e.message}"))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("创建用户异常: ${e.message}", e)
            ResponseEntity.ok(ApiResponse.serverError("创建用户失败: ${e.message}"))
        }
    }
    
    /**
     * 更新用户密码
     */
    @PostMapping("/update-password")
    fun updateUserPassword(
        @RequestBody requestBody: UserUpdatePasswordRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val error = checkDefaultUser(httpRequest)
            if (error != null) {
                return ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_PERMISSION_DENIED.code, error))
            }
            
            val currentUsername = getCurrentUsername(httpRequest)!!
            val result = userService.updateUserPassword(requestBody, currentUsername)
            
            result.fold(
                onSuccess = {
                    ResponseEntity.ok(ApiResponse.success(Unit))
                },
                onFailure = { e ->
                    logger.error("更新用户密码失败: ${e.message}", e)
                    when (e) {
                        is IllegalArgumentException -> ResponseEntity.ok(ApiResponse.paramError(e.message ?: "参数错误"))
                        is IllegalStateException -> ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_PERMISSION_DENIED.code, e.message ?: "权限不足"))
                        else -> ResponseEntity.ok(ApiResponse.serverError("更新用户密码失败: ${e.message}"))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("更新用户密码异常: ${e.message}", e)
            ResponseEntity.ok(ApiResponse.serverError("更新用户密码失败: ${e.message}"))
        }
    }
    
    /**
     * 用户修改自己的密码
     */
    @PostMapping("/update-own-password")
    fun updateOwnPassword(
        @RequestBody requestBody: UserUpdateOwnPasswordRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val currentUsername = getCurrentUsername(httpRequest)
            if (currentUsername == null) {
                return ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_ERROR.code, "未获取到用户信息"))
            }
            
            val result = userService.updateOwnPassword(requestBody.newPassword, currentUsername)
            
            result.fold(
                onSuccess = {
                    ResponseEntity.ok(ApiResponse.success(Unit))
                },
                onFailure = { e ->
                    logger.error("修改自己密码失败: ${e.message}", e)
                    when (e) {
                        is IllegalArgumentException -> ResponseEntity.ok(ApiResponse.paramError(e.message ?: "参数错误"))
                        else -> ResponseEntity.ok(ApiResponse.serverError("修改密码失败: ${e.message}"))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("修改自己密码异常: ${e.message}", e)
            ResponseEntity.ok(ApiResponse.serverError("修改密码失败: ${e.message}"))
        }
    }
    
    /**
     * 删除用户
     */
    @PostMapping("/delete")
    fun deleteUser(
        @RequestBody requestBody: UserDeleteRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val error = checkDefaultUser(httpRequest)
            if (error != null) {
                return ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_PERMISSION_DENIED.code, error))
            }
            
            val currentUsername = getCurrentUsername(httpRequest)!!
            val result = userService.deleteUser(requestBody.userId, currentUsername)
            
            result.fold(
                onSuccess = {
                    ResponseEntity.ok(ApiResponse.success(Unit))
                },
                onFailure = { e ->
                    logger.error("删除用户失败: ${e.message}", e)
                    when (e) {
                        is IllegalArgumentException -> ResponseEntity.ok(ApiResponse.paramError(e.message ?: "参数错误"))
                        is IllegalStateException -> ResponseEntity.ok(ApiResponse.error(ErrorCode.AUTH_PERMISSION_DENIED.code, e.message ?: "权限不足"))
                        else -> ResponseEntity.ok(ApiResponse.serverError("删除用户失败: ${e.message}"))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("删除用户异常: ${e.message}", e)
            ResponseEntity.ok(ApiResponse.serverError("删除用户失败: ${e.message}"))
        }
    }
}

