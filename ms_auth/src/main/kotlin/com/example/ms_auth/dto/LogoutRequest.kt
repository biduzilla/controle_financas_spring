package com.example.ms_auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Payload para logout")
data class LogoutRequest(
    @field:NotBlank
    @Schema(description = "Refresh token a ser revogado", required = true)
    val refreshToken: String
)