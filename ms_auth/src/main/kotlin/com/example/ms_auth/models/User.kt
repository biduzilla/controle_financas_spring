package com.example.ms_auth.models

import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(columnNames = ["email", "deleted", "updatedAt"])])
@SQLRestriction("deleted <> true")
@SQLDelete(sql = "UPDATE USERS SET deleted = true, updatedAt = NOW() WHERE id = ?")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var email: String,
    var passwordHash: String,
    var name: String,
) : BaseModel(), UserDetails {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 31

    override fun toString(): String {
        return "User(id=$id, email='$email', name='$name')"
    }

    override fun getAuthorities(): Collection<GrantedAuthority> =
        emptyList()

    override fun getPassword(): String? = passwordHash
    override fun getUsername(): String = email
}