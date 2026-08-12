package com.example.ms_auth.repositories

import com.example.ms_auth.models.RefreshToken
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshToken?

    @Query(
        """
        SELECT rt FROM RefreshToken rt 
        WHERE rt.family = :family 
          AND rt.revoked = false 
          AND rt.expiresAt > :now
    """
    )
    fun findActiveByFamily(@Param("family") family: String, @Param("now") now: Instant): List<RefreshToken>

    @Modifying
    @Transactional
    @Query(
        """
        UPDATE RefreshToken rt 
        SET rt.revoked = true 
        WHERE rt.family = :family
    """
    )
    fun revokeAllByFamily(@Param("family") family: String): Int
}