package com.example.ms_auth.configs

import com.example.ms_auth.exceptions.InvalidTokenException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class CustomAuthenticationEntryPoint(
    private val handlerExceptionResolver: HandlerExceptionResolver
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {

        handlerExceptionResolver.resolveException(
            request,
            response,
            null,
            InvalidTokenException(
                "Token ausente ou inválido"
            )
        )
    }
}