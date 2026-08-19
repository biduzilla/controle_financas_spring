package com.example.ms_category.security

import com.example.ms_category.exceptions.InvalidTokenException
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

interface IJwtService {
    fun extractUsername(token: String): String
    fun extractAuthenticatedUser(token: String): AuthenticatedUser
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T
    fun isTokenValid(token: String, userDetails: UserDetails): Boolean
}

@Service
class JwtService(
    @Value($$"${security.jwt.public-key}") private val publicKeyResource: Resource,
) : IJwtService {
    private val publicKey: PublicKey by lazy { loadPublicKey() }

    override fun extractUsername(token: String): String =
        extractClaim(token, Claims::getSubject)

    override fun extractAuthenticatedUser(token: String): AuthenticatedUser {
        val claims = extractAllClaims(token)
        val roles = (claims["roles"] as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList()

        return AuthenticatedUser(
            userId = UUID.fromString(claims["userId"] as String),
            email = claims.subject,
            name = claims["name"] as? String ?: "",
            roles = roles
        )
    }

    override fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T =
        claimsResolver(extractAllClaims(token))


    override fun isTokenValid(
        token: String,
        userDetails: UserDetails
    ): Boolean {
        val userName: String = extractUsername(token)
        return userName == userDetails.username && !isTokenExpired(token)
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun isTokenExpired(token: String): Boolean =
        extractExpiration(token).before(Date())

    private fun extractAllClaims(token: String): Claims =
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            throw InvalidTokenException("Token expirado")
        } catch (e: MalformedJwtException) {
            throw InvalidTokenException("Token malformado")
        } catch (e: UnsupportedJwtException) {
            throw InvalidTokenException("Token não suportado")
        } catch (e: IllegalArgumentException) {
            throw InvalidTokenException("Token vazio ou nulo")
        } catch (e: JwtException) {
            throw InvalidTokenException("Token inválido")
        }

    private fun loadPublicKey(): PublicKey {
        val pem = publicKeyResource.inputStream.bufferedReader().readText()
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(pem)
        return KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(keyBytes))
    }
}