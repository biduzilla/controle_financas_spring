package com.example.ms_auth.models

import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
@SQLRestriction("deleted <> true")
@SQLDelete(sql = "UPDATE refresh_tokens SET deleted = true, updatedAt = NOW() WHERE id = ?")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true, length = 64)
    var tokenHash: String,

    @Column(nullable = false)
    var userId: UUID,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false)
    var issuedAt: Instant = Instant.now(),

    @Column(length = 36)
    var family: String? = null,

    @Column(nullable = false)
    var revoked: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RefreshToken) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 31

    override fun toString(): String {
        return "RefreshToken(id=$id, userId=$userId, expiresAt=$expiresAt, revoked=$revoked)"
    }
}