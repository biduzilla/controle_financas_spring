package com.example.ms_category.dto

import com.example.ms_category.models.Category
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.*

data class CategoryRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String,

    @field:NotBlank(message = "Type is required")
    @field:Pattern(regexp = "input|output", message = "Type must be 'input' or 'output'")
    val type: String,

    val goalId: UUID?
)

data class CategoryResponse(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val type: String,
    val goalId: UUID?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(category: Category): CategoryResponse {
            return CategoryResponse(
                id = category.id!!,
                userId = category.userId!!,
                name = category.name,
                type = category.type.value,
                goalId = category.goalId,
                createdAt = category.createdAt,
                updatedAt = category.updatedAt
            )
        }
    }
}