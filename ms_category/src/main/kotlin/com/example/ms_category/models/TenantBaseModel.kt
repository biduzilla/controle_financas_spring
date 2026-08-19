package com.example.ms_category.models

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.TenantId
import java.util.*

@MappedSuperclass
abstract class TenantBaseModel : BaseModel() {

    @TenantId
    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID? = null
}