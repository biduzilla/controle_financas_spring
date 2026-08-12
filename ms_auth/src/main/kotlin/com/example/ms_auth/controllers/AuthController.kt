package com.example.ms_auth.controllers

import com.example.ms_auth.dto.AuthResponse
import com.example.ms_auth.dto.LoginRequest
import com.example.ms_auth.dto.LogoutRequest
import com.example.ms_auth.dto.RefreshTokenRequest
import com.example.ms_auth.services.IAuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticação e gerenciamento de tokens")
class AuthController(
    private val authService: IAuthService
) {

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Gera access token e refresh token")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Autenticação bem-sucedida",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Credenciais inválidas"),
            ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos")
        ]
    )
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<AuthResponse> {
        val authResponse = authService.authenticate(request)
        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Renovar token de acesso",
        description = "Usa um refresh token válido para gerar um novo par de tokens (rotação)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Novo par de tokens gerado",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Refresh token inválido, revogado ou expirado")
        ]
    )
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<AuthResponse> {
        val authResponse = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Encerrar sessão",
        description = "Revoga o refresh token fornecido e todos os tokens da mesma família"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            ApiResponse(responseCode = "400", description = "Refresh token inválido")
        ]
    )
    fun logout(
        @Valid @RequestBody request: LogoutRequest
    ): ResponseEntity<Void> {
        authService.logout(request.refreshToken)
        return ResponseEntity.noContent().build()
    }
}