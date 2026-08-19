package com.example.ms_category.repositories

import com.example.ms_category.models.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {
    @Query(
        """
        SELECT c FROM Category c
        WHERE (
            :search IS NULL
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
        )
    """
    )
    fun findAllBySearch(
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<Category>
}