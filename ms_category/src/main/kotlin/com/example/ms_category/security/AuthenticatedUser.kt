package com.example.ms_category.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class AuthenticatedUser(
    val userId: UUID,
    val email: String,
    val name: String,
    val roles: List<String>
) : UserDetails {
    override fun getUsername() = email
    override fun getPassword() = null
    override fun getAuthorities() = roles.map { SimpleGrantedAuthority(it) }
    override fun isEnabled() = true
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
}
