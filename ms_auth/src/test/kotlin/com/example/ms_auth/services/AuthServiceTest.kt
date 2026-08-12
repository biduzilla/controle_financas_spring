package com.example.ms_auth.services

import com.example.ms_auth.dto.LoginRequest
import com.example.ms_auth.exceptions.BadCredentialsException
import com.example.ms_auth.exceptions.BadRequestException
import com.example.ms_auth.models.RefreshToken
import com.example.ms_auth.models.User
import com.example.ms_auth.repositories.RefreshTokenRepository
import com.example.ms_auth.security.IJwtService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*   // <-- único import de matchers e funções
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var jwtService: IJwtService

    @Mock
    private lateinit var userService: IUserService

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @InjectMocks
    private lateinit var authService: AuthService

    private fun createUser(
        id: UUID = UUID.randomUUID(),
        email: String = "test@email.com",
        passwordHash: String = "hash",
        name: String = "Test User"
    ) = User(
        id = id,
        email = email,
        passwordHash = passwordHash,
        name = name
    )

    private fun createRefreshToken(
        id: UUID = UUID.randomUUID(),
        tokenHash: String = "someHash",
        userId: UUID = UUID.randomUUID(),
        expiresAt: Instant = Instant.now().plusSeconds(3600),
        issuedAt: Instant = Instant.now(),
        family: String? = UUID.randomUUID().toString(),
        revoked: Boolean = false
    ) = RefreshToken(
        id = id,
        tokenHash = tokenHash,
        userId = userId,
        expiresAt = expiresAt,
        issuedAt = issuedAt,
        family = family,
        revoked = revoked
    )

    private fun hashToken(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `authenticate should return tokens and save refresh token on success`() {
        val loginRequest = LoginRequest(login = "test@email.com", password = "Password1")
        val userDetails: UserDetails = mock()
        val user = createUser(email = loginRequest.login)

        val authentication: Authentication = mock()
        whenever(authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()))
            .thenReturn(authentication)
        whenever(authentication.principal).thenReturn(userDetails)
        whenever(userDetails.username).thenReturn(loginRequest.login)

        whenever(userService.findByEmail(loginRequest.login)).thenReturn(user)

        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        whenever(jwtService.generateToken(user)).thenReturn(accessToken)
        whenever(jwtService.generateRefreshToken(user)).thenReturn(refreshToken)
        whenever(jwtService.getExpirationTime()).thenReturn(900L)
        whenever(jwtService.getRefreshExpirationTime()).thenReturn(604800000L)

        val result = authService.authenticate(loginRequest)

        assertEquals(accessToken, result.accessToken)
        assertEquals(refreshToken, result.refreshToken)
        assertEquals(900L, result.expiresIn)

        argumentCaptor<RefreshToken>().apply {
            verify(refreshTokenRepository).save(capture())
            val saved = firstValue
            assertEquals(user.id, saved.userId)
            assertEquals(hashToken(refreshToken), saved.tokenHash)
            assertNotNull(saved.family)
            assertFalse(saved.revoked)
            assertTrue(saved.expiresAt.isAfter(Instant.now()))
        }
    }

    @Test
    fun `authenticate should throw BadCredentialsException when authentication fails`() {
        val loginRequest = LoginRequest(login = "wrong@email.com", password = "WrongPassword")
        whenever(authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()))
            .thenThrow(BadCredentialsException("Invalid email or password"))

        assertThrows<BadCredentialsException> {
            authService.authenticate(loginRequest)
        }

        verify(userService, never()).findByEmail(any())
        verify(jwtService, never()).generateToken(any())
        verify(jwtService, never()).generateRefreshToken(any())
        verify(refreshTokenRepository, never()).save(any())
    }

    @Test
    fun `refreshToken should rotate token and return new pair on success`() {
        val oldRefreshToken = "old-refresh-token"
        val username = "test@email.com"
        val user = createUser(email = username)
        val oldStoredToken = createRefreshToken(
            tokenHash = hashToken(oldRefreshToken),
            userId = user.id!!,
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = false
        )

        whenever(jwtService.extractUsername(oldRefreshToken)).thenReturn(username)
        whenever(userService.findByEmail(username)).thenReturn(user)
        whenever(jwtService.isRefreshTokenValid(oldRefreshToken, user)).thenReturn(true)
        whenever(refreshTokenRepository.findByTokenHash(hashToken(oldRefreshToken)))
            .thenReturn(oldStoredToken)

        val newAccessToken = "new-access"
        val newRefreshToken = "new-refresh"
        whenever(jwtService.generateToken(user)).thenReturn(newAccessToken)
        whenever(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken)
        whenever(jwtService.getExpirationTime()).thenReturn(900L)
        whenever(jwtService.getRefreshExpirationTime()).thenReturn(604800000L)

        val result = authService.refreshToken(oldRefreshToken)

        assertEquals(newAccessToken, result.accessToken)
        assertEquals(newRefreshToken, result.refreshToken)
        assertEquals(900L, result.expiresIn)

        assertTrue(oldStoredToken.revoked)
        verify(refreshTokenRepository).save(oldStoredToken)

        argumentCaptor<RefreshToken>().apply {
            verify(refreshTokenRepository, times(2)).save(capture())
            val savedTokens = allValues
            val savedNew = savedTokens.last()
            assertEquals(user.id, savedNew.userId)
            assertEquals(hashToken(newRefreshToken), savedNew.tokenHash)
            assertEquals(oldStoredToken.family, savedNew.family)
            assertFalse(savedNew.revoked)
        }
    }

    @Test
    fun `refreshToken should throw BadRequestException when token not found in database`() {
        val refreshToken = "non-existent-token"
        val username = "test@email.com"
        val user = createUser(email = username)

        whenever(jwtService.extractUsername(refreshToken)).thenReturn(username)
        whenever(userService.findByEmail(username)).thenReturn(user)
        whenever(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(true)
        whenever(refreshTokenRepository.findByTokenHash(hashToken(refreshToken))).thenReturn(null)

        assertThrows<BadRequestException> {
            authService.refreshToken(refreshToken)
        }

        verify(jwtService, never()).generateToken(any())
        verify(jwtService, never()).generateRefreshToken(any())
        verify(refreshTokenRepository, never()).save(any())
    }

    @Test
    fun `refreshToken should throw BadRequestException when token is revoked and revoke all family`() {
        val refreshToken = "revoked-token"
        val username = "test@email.com"
        val user = createUser(email = username)
        val family = "family-123"
        val revokedToken = createRefreshToken(
            tokenHash = hashToken(refreshToken),
            userId = user.id!!,
            family = family,
            revoked = true
        )

        whenever(jwtService.extractUsername(refreshToken)).thenReturn(username)
        whenever(userService.findByEmail(username)).thenReturn(user)
        whenever(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(true)
        whenever(refreshTokenRepository.findByTokenHash(hashToken(refreshToken)))
            .thenReturn(revokedToken)

        assertThrows<BadRequestException> {
            authService.refreshToken(refreshToken)
        }

        verify(refreshTokenRepository).revokeAllByFamily(family)
        verify(refreshTokenRepository, never()).save(any())
    }

    @Test
    fun `refreshToken should throw BadRequestException when token is expired`() {
        val refreshToken = "expired-token"
        val username = "test@email.com"
        val user = createUser(email = username)
        val expiredToken = createRefreshToken(
            tokenHash = hashToken(refreshToken),
            userId = user.id!!,
            expiresAt = Instant.now().minusSeconds(60),
            revoked = false
        )

        whenever(jwtService.extractUsername(refreshToken)).thenReturn(username)
        whenever(userService.findByEmail(username)).thenReturn(user)
        whenever(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(true)
        whenever(refreshTokenRepository.findByTokenHash(hashToken(refreshToken)))
            .thenReturn(expiredToken)

        assertThrows<BadRequestException> {
            authService.refreshToken(refreshToken)
        }

        verify(refreshTokenRepository, never()).save(any())
        verify(refreshTokenRepository, never()).revokeAllByFamily(any())
    }

    @Test
    fun `refreshToken should throw BadRequestException when JWT validation fails`() {
        val refreshToken = "invalid-token"
        val username = "test@email.com"
        val user = createUser(email = username)

        whenever(jwtService.extractUsername(refreshToken)).thenReturn(username)
        whenever(userService.findByEmail(username)).thenReturn(user)
        whenever(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(false)

        assertThrows<BadRequestException> {
            authService.refreshToken(refreshToken)
        }

        verify(refreshTokenRepository, never()).findByTokenHash(any())
        verify(refreshTokenRepository, never()).save(any())
    }

    @Test
    fun `logout should revoke token and revoke all family when token exists`() {
        val refreshToken = "logout-token"
        val storedToken = createRefreshToken(
            tokenHash = hashToken(refreshToken),
            family = "some-family",
            revoked = false
        )

        whenever(refreshTokenRepository.findByTokenHash(hashToken(refreshToken)))
            .thenReturn(storedToken)

        authService.logout(refreshToken)

        assertTrue(storedToken.revoked)
        verify(refreshTokenRepository).save(storedToken)
        verify(refreshTokenRepository).revokeAllByFamily(storedToken.family!!)
    }

    @Test
    fun `logout should do nothing when token is not found`() {
        val refreshToken = "unknown-token"

        whenever(refreshTokenRepository.findByTokenHash(hashToken(refreshToken)))
            .thenReturn(null)

        authService.logout(refreshToken)

        verify(refreshTokenRepository, never()).save(any())
        verify(refreshTokenRepository, never()).revokeAllByFamily(any())
    }

    @Test
    fun `logout should revoke token but not call revokeAllByFamily if family is null`() {
        val refreshToken = "no-family-token"
        val storedToken = createRefreshToken(
            tokenHash = hashToken(refreshToken),
            family = null,
            revoked = false
        )

        whenever(refreshTokenRepository.findByTokenHash(hashToken(refreshToken)))
            .thenReturn(storedToken)

        authService.logout(refreshToken)

        assertTrue(storedToken.revoked)
        verify(refreshTokenRepository).save(storedToken)
        verify(refreshTokenRepository, never()).revokeAllByFamily(any())
    }
}