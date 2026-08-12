package com.example.ms_auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Payload para renovar tokens")
data class RefreshTokenRequest(
    @field:NotBlank
    @Schema(description = "Refresh token atual", required = true)
    val refreshToken: String
)