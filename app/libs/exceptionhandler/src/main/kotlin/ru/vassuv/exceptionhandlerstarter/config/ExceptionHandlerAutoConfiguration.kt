package ru.vassuv.exceptionhandlerstarter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import ru.vassuv.exceptionhandlerstarter.handler.GlobalExceptionHandler

@Configuration
open class ExceptionHandlerAutoConfiguration {

    init {
        println("✅ ExceptionHandlerAutoConfiguration from starter initialized")
    }

    @Bean
    @ConditionalOnMissingBean
    open fun globalExceptionHandler(): GlobalExceptionHandler = GlobalExceptionHandler().also {
        println("✅ Registering GlobalExceptionHandler from starter")
    }
}