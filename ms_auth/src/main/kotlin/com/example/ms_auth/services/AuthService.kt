package com.example.ms_auth.services

import com.example.ms_auth.dto.AuthResponse
import com.example.ms_auth.dto.LoginRequest
import com.example.ms_auth.exceptions.BadCredentialsException
import com.example.ms_auth.exceptions.BadRequestException
import com.example.ms_auth.models.RefreshToken
import com.example.ms_auth.repositories.RefreshTokenRepository
import com.example.ms_auth.security.IJwtService
import jakarta.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.*

interface IAuthService {
    fun authenticate(request: LoginRequest): AuthResponse
    fun refreshToken(refreshToken: String): AuthResponse
    fun logout(refreshToken: String)
}

@Service
class AuthService(
    private val jwtService: IJwtService,
    private val userService: IUserService,
    private val authenticationManager: AuthenticationManager,
    private val refreshTokenRepository: RefreshTokenRepository
) : IAuthService {
    override fun authenticate(request: LoginRequest): AuthResponse {
        return try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    request.login,
                    request.password
                )
            )

            val userDetails = auth.principal as UserDetails
            val user = userService.findByEmail(userDetails.username)
            val accessToken = jwtService.generateToken(user)
            val refreshToken = jwtService.generateRefreshToken(user)

            val family = UUID.randomUUID().toString()
            saveRefreshToken(user.id!!, refreshToken, family)

            AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = jwtService.getExpirationTime()
            )
        } catch (e: AuthenticationException) {
            throw BadCredentialsException("Invalid email or password")
        }
    }

    override fun refreshToken(refreshToken: String): AuthResponse {
        val username = jwtService.extractUsername(refreshToken)
        val user = userService.findByEmail(username)

        if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
            throw BadRequestException("Invalid refresh token")
        }

        val tokenHash = hashToken(refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw BadRequestException("Refresh token not found")

        if (storedToken.revoked) {
            storedToken.family?.let { family ->
                refreshTokenRepository.revokeAllByFamily(family)
            }
            throw BadRequestException("Refresh token has been revoked. Please login again.")
        }

        if (storedToken.expiresAt.isBefore(Instant.now())) {
            throw BadRequestException("Refresh token expired")
        }

        storedToken.revoked = true
        refreshTokenRepository.save(storedToken)

        val newAccessToken = jwtService.generateToken(user)
        val newRefreshToken = jwtService.generateRefreshToken(user)

        saveRefreshToken(user.id!!, newRefreshToken, storedToken.family)

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = jwtService.getExpirationTime()
        )
    }

    @Transactional
    override fun logout(refreshToken: String) {
        val tokenHash = hashToken(refreshToken)
        val storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: return

        storedToken.revoked = true
        refreshTokenRepository.save(storedToken)

        storedToken.family?.let { family ->
            refreshTokenRepository.revokeAllByFamily(family)
        }
    }

    private fun saveRefreshToken(userId: UUID, refreshToken: String, family: String?) {
        val now = Instant.now()
        val tokenHash = hashToken(refreshToken)
        val expiresAt = now.plusMillis(jwtService.getRefreshExpirationTime())

        val entity = RefreshToken(
            tokenHash = tokenHash,
            userId = userId,
            expiresAt = expiresAt,
            issuedAt = now,
            family = family,
            revoked = false
        )
        refreshTokenRepository.save(entity)
    }

    private fun hashToken(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}