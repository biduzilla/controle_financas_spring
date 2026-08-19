package com.example.ms_category.models

import com.example.ms_category.enums.CategoryTypeEnum
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.util.*

@Entity
@Table(name = "categories")
@SQLRestriction("deleted <> true")
@SQLDelete(sql = "UPDATE categories SET deleted = true, updated_at = NOW() WHERE id = ?")class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var type: CategoryTypeEnum,

    @Column(name = "goal_id")
    var goalId: UUID? = null,
) : TenantBaseModel()