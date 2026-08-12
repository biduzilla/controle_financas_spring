package com.example.ms_auth.utils

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.JpaSort

fun orderByToSort(orderBy: String?): Sort {
    if (orderBy.isNullOrBlank()) {
        return Sort.by(Sort.Direction.DESC, "updatedAt")
    }

    var sort: Sort = Sort.unsorted()

    orderBy.split(",").forEach { part ->
        val trimmed = part.trim()
        if (trimmed.isBlank()) {
            return@forEach
        }

        val parts = trimmed.split("\\s+".toRegex())
        val field = parts[0].trim()
        val directionStr = if (parts.size > 1) parts[1].trim() else "asc"
        val direction = Sort.Direction.valueOf(directionStr.uppercase())
        val current = JpaSort.unsafe(direction, field)
        sort = if (sort.isUnsorted) current else sort.and(current)
    }

    return if (sort.isUnsorted) {
        Sort.by(Sort.Direction.DESC, "updatedAt")
    } else {
        sort
    }
}