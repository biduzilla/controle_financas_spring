package com.example.ms_auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Payload de login")
data class LoginRequest(
    @field:NotBlank
    @Schema(description = "E-mail ou username", example = "vet@exemplo.com", required = true)
    val login: String,

    @field:NotBlank
    @Schema(description = "Senha", example = "Senha@123", required = true)
    val password: String
)