package com.example.ms_auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Estrutura padronizada de erro da API")
data class ErrorDTO(

    @Schema(example = "2026-06-25T14:30:00")
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @Schema(example = "422")
    val status: Int,

    @Schema(example = "Unprocessable Entity")
    val error: String,

    @Schema(example = "Erro de validação nos campos de entrada.")
    val message: String?,

    @Schema(example = "/api/products")
    val path: String,

    @Schema(
        description = "Detalhes dos campos com erro (campo -> mensagem)",
        example = """{"name": "Name must not be empty", "price": "Price must be greater than 0"}"""
    )
    val details: Map<String, String>? = null
)