package com.example.ms_category.models

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseModel(
    @field:CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @field:CreatedBy
    @Column(nullable = false, updatable = false)
    var createdBy: String? = null,
    @field:LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null,
    @field:LastModifiedBy
    @Column(nullable = false)
    var updatedBy: String? = null,
    var deleted: Boolean = false
)