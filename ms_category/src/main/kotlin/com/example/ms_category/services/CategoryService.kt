package com.example.ms_category.services

import com.example.ms_category.dto.CategoryRequest
import com.example.ms_category.enums.CategoryTypeEnum
import com.example.ms_category.exceptions.BadRequestException
import com.example.ms_category.exceptions.NotFoundException
import com.example.ms_category.models.Category
import com.example.ms_category.repositories.CategoryRepository
import com.example.ms_category.utils.CacheConstants
import com.example.ms_category.utils.orderByToSort
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

interface CategoryService {
    fun findById(id: UUID): Category
    fun findAll(search: String?, page: Int, size: Int, orderBy: String?): Page<Category>
    fun create(request: CategoryRequest): Category
    fun update(id: UUID, request: CategoryRequest): Category
    fun delete(id: UUID)
}

@Service
class CategoryServiceImpl(
    private val repository: CategoryRepository
) : CategoryService {

    @Cacheable(cacheNames = [CacheConstants.CATEGORY_BY_ID], keyGenerator = "customKeyGenerator")
    override fun findById(id: UUID): Category {
        return repository.findById(id).orElseThrow{NotFoundException("Category not found")}
    }

    override fun findAll(search: String?, page: Int, size: Int, orderBy: String?): Page<Category> {
        val sort = orderByToSort(orderBy)
        val pageable = PageRequest.of(page, size, sort)
        return repository.findAllBySearch(search, pageable)
    }

    @Transactional
    @CacheEvict(cacheNames = [CacheConstants.CATEGORY_BY_ID, CacheConstants.CATEGORY_LIST], allEntries = true)
    override fun create(request: CategoryRequest): Category {
        val type = parseType(request.type)
        val category = Category(
            name = request.name,
            type = type,
            goalId = request.goalId
        )
        return repository.save(category)
    }

    @Transactional
    @CacheEvict(cacheNames = [CacheConstants.CATEGORY_BY_ID, CacheConstants.CATEGORY_LIST], allEntries = true)
    override fun update(id: UUID, request: CategoryRequest): Category {
        val existing = repository.findById(id).orElseThrow{NotFoundException("Category not found")}

        existing.name = request.name
        existing.type = parseType(request.type)
        existing.goalId = request.goalId

        return repository.save(existing)
    }

    @Transactional
    @CacheEvict(cacheNames = [CacheConstants.CATEGORY_BY_ID, CacheConstants.CATEGORY_LIST], allEntries = true)
    override fun delete(id: UUID) {
        val category = repository.findById(id).orElseThrow { NotFoundException("Category not found") }
        repository.delete(category)
    }

    private fun parseType(value: String): CategoryTypeEnum {
        return CategoryTypeEnum.fromValue(value)
            ?: throw BadRequestException("Invalid category type: $value")
    }
}