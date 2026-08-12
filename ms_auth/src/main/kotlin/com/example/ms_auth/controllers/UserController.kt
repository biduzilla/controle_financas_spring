package com.example.ms_auth.controllers

import com.example.ms_auth.dto.CreateUserRequest
import com.example.ms_auth.dto.UpdateUserRequest
import com.example.ms_auth.dto.UserResponse
import com.example.ms_auth.dto.toResponse
import com.example.ms_auth.services.IUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operações de gerenciamento de usuários")
class UserController(
    private val userService: IUserService
) {

    @PostMapping
    @Operation(summary = "Criar um novo usuário", description = "Registra um usuário com senha validada")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Usuário criado com sucesso",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado")
        ]
    )
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.signUp(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user.toResponse())
    }

    @GetMapping
    @Operation(
        summary = "Listar usuários com paginação e busca",
        description = "Retorna uma página de usuários, com opção de ordenação e filtro por nome/e-mail"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Página de usuários retornada")
        ]
    )
    fun listUsers(
        @Parameter(description = "Número da página (começa em 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Quantidade de itens por página", example = "10")
        @RequestParam(defaultValue = "10") size: Int,

        @Parameter(description = "Critério de ordenação (ex.: 'email,ASC' ou 'name,DESC')")
        @RequestParam(required = false) orderBy: String?,

        @Parameter(description = "Termo de busca para filtrar por nome ou e-mail")
        @RequestParam(required = false) search: String?
    ): ResponseEntity<Page<UserResponse>> {
        val users = userService.findAll(page, size, orderBy, search)
        val responsePage = users.map { it.toResponse() }
        return ResponseEntity.ok(responsePage)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados públicos de um usuário específico")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Usuário encontrado",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        ]
    )
    fun getUserById(
        @Parameter(description = "UUID do usuário", required = true)
        @PathVariable id: UUID
    ): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping("/email/{email}")
    @Operation(
        summary = "Buscar usuário por e-mail",
        description = "Retorna os dados públicos de um usuário pelo endereço de e-mail"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Usuário encontrado",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        ]
    )
    fun getUserByEmail(
        @Parameter(description = "E-mail do usuário", required = true)
        @PathVariable email: String
    ): ResponseEntity<UserResponse> {
        val user = userService.findByEmail(email)
        return ResponseEntity.ok(user.toResponse())
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar dados do usuário",
        description = "Atualiza e-mail, nome ou senha (se informada). Todos os campos são opcionais."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Usuário atualizado com sucesso",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            ApiResponse(responseCode = "400", description = "Dados inválidos")
        ]
    )
    fun updateUser(
        @Parameter(description = "UUID do usuário a ser atualizado", required = true)
        @PathVariable id: UUID,

        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.update(id, request)
        return ResponseEntity.ok(updatedUser.toResponse())
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário", description = "Remove permanentemente o usuário do sistema")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        ]
    )
    fun deleteUser(
        @Parameter(description = "UUID do usuário a ser excluído", required = true)
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        userService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}