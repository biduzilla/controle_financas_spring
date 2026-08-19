package com.example.ms_category.enums

enum class CategoryTypeEnum(val value: String) {
    INPUT("input"),
    OUTPUT("output");

    companion object {
        fun fromValue(value: String): CategoryTypeEnum? =
            entries.find { it.value == value }
    }
}