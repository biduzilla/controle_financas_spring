package com.example.ms_auth.configs

import com.example.ms_auth.exceptions.InvalidTokenException
import com.example.ms_auth.security.IJwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class JwtAuthenticationFilter(
    private val jwtService: IJwtService,
    private val userDetailsService: UserDetailsService,
    private val handlerExceptionResolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader: String = request.getHeader("Authorization") ?: ""

        if (authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val jwt = authHeader.substring(7)
            val userEmail = jwtService.extractUsername(jwt)

            val auth = SecurityContextHolder.getContext().authentication

            if (userEmail.isNotBlank() && auth == null) {
                val userDetails = userDetailsService.loadUserByUsername(userEmail)

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                } else {
                    throw InvalidTokenException("Token not valid for user")
                }
            }
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            handlerExceptionResolver
                .resolveException(
                    request,
                    response,
                    null,
                    e
                )
        }
    }

//    private fun sendErrorResponse(
//        request: HttpServletRequest,
//        response: HttpServletResponse,
//        status: HttpStatus,
//        message: String
//    ) {
//        response.status = status.value()
//        response.contentType = MediaType.APPLICATION_JSON_VALUE
//
//        val errorDTO = ErrorDTO(
//            status = status.value(),
//            error = status.name,
//            message = message,
//            path = request.requestURI,
//            timestamp = LocalDateTime.now()
//        )
//
//        objectMapper.writeValue(response.writer, errorDTO)
//    }
}