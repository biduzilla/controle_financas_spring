package com.example.ms_auth.configs

import com.example.ms_auth.services.IUserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ApplicationConfiguration(
    private val userService: IUserService,
    private val passwordEncoder: PasswordEncoder
) {
    @Bean
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username ->
            userService.findByEmail(username)
        }
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider(userDetailsService())
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }
}