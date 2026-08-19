package com.example.ms_category.controllers

import com.example.ms_category.dto.CategoryRequest
import com.example.ms_category.dto.CategoryResponse
import com.example.ms_category.services.CategoryService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val service: CategoryService
) {

    @PostMapping
    fun create(@Valid @RequestBody request: CategoryRequest): ResponseEntity<CategoryResponse> {
        val category = service.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.fromEntity(category))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<CategoryResponse> {
        val category = service.findById(id)
        return ResponseEntity.ok(CategoryResponse.fromEntity(category))
    }

    @GetMapping
    fun findAll(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) orderBy: String?
    ): ResponseEntity<Page<CategoryResponse>> {
        val categories = service.findAll(search, page, size, orderBy)
        val responsePage = categories.map { CategoryResponse.fromEntity(it) }
        return ResponseEntity.ok(responsePage)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CategoryRequest
    ): ResponseEntity<CategoryResponse> {
        val category = service.update(id, request)
        return ResponseEntity.ok(CategoryResponse.fromEntity(category))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}