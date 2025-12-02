package com.wrbug.polymarketbot.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC 配置
 * 注册JWT认证拦截器
 */
@Configuration
class WebMvcConfig(
    private val jwtAuthenticationInterceptor: JwtAuthenticationInterceptor
) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
            .addPathPatterns("/api/**")
    }
}

