package com.example.ms_category.configs

import com.example.ms_category.exceptions.InvalidTokenException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication? {
        if (authentication is UsernamePasswordAuthenticationToken
            && authentication.isAuthenticated
        ) {
            return authentication
        }
        throw InvalidTokenException("Autentication not suported")
    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}