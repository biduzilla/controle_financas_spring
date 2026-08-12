package com.example.ms_auth.dto

import com.example.ms_auth.models.User
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "Payload para criar um novo usuário")
data class CreateUserRequest(

    @field:NotBlank
    @field:Email
    @Schema(description = "E-mail válido", example = "vet@exemplo.com", required = true)
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 100)
    @Schema(description = "Senha em texto puro (será hasheada)", example = "Senha@123", minLength = 8, maxLength = 100, required = true)
    val password: String,

    @field:NotBlank
    @field:Size(min = 2, max = 150)
    @Schema(description = "Nome completo", example = "Dra. Mariana Souza", required = true)
    val name: String
)

@Schema(description = "Payload para atualizar dados do usuário (todos os campos opcionais)")
data class UpdateUserRequest(

    @field:Email
    @Schema(description = "Novo e-mail (se informado, deve ser válido)", example = "novo@email.com")
    val email: String? = null,

    @field:Size(min = 8, max = 100)
    @Schema(description = "Nova senha (se informada, mínimo 8 caracteres)", example = "Nova@123")
    val password: String? = null,

    @field:Size(min = 2, max = 150)
    @Schema(description = "Novo nome", example = "Dra. Mariana S. Costa")
    val name: String? = null
)

@Schema(description = "Representação pública de um usuário (sem dados sensíveis)")
data class UserResponse(

    @Schema(description = "Identificador único", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @Schema(description = "E-mail", example = "vet@exemplo.com")
    val email: String,

    @Schema(description = "Nome completo", example = "Dra. Mariana Souza")
    val name: String,

    @Schema(description = "Data de criação do registro")
    val createdAt: LocalDateTime?,

    @Schema(description = "Data da última atualização")
    val updatedAt: LocalDateTime?
)

fun User.toResponse() = UserResponse(
    id = id!!,
    email = email,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt
)