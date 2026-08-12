package com.example.ms_auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Resposta de autenticação com tokens")
data class AuthResponse(
    @Schema(description = "Token de acesso JWT")
    val accessToken: String,

    @Schema(description = "Refresh token JWT")
    val refreshToken: String,

    @Schema(description = "Tempo de expiração do access token em segundos")
    val expiresIn: Long
)