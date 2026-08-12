package com.example.ms_auth.models

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
    @Column(insertable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @field:CreatedBy
    @Column(insertable = false)
    var createdBy: String? = null,
    @field:LastModifiedDate
    @Column(insertable = false)
    var updatedAt: LocalDateTime? = null,
    @field:LastModifiedBy
    @Column(insertable = false)
    var updatedBy: String? = null,
    var deleted: Boolean = false
)