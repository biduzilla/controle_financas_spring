package com.example.ms_auth.repositories

import com.example.ms_auth.models.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository: JpaRepository<RefreshToken, UUID> {
}