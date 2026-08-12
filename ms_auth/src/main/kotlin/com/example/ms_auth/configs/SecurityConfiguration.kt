package com.example.ms_auth.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val authenticationProvider: AuthenticationProvider,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint
) {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.run {
            csrf { it.disable() }
            authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()

                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/user"
                    ).permitAll()

                    .anyRequest().authenticated()
            }
            sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            exceptionHandling { exception ->
                exception.authenticationEntryPoint(customAuthenticationEntryPoint)
            }
            authenticationProvider(authenticationProvider)

            addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        }

        return http.build()
    }
}