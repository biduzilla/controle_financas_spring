package com.example.ms_category.utils

import com.example.ms_auth.annotations.NotCacheKey
import com.example.ms_category.annotations.NotCacheKey
import org.springframework.cache.interceptor.KeyGenerator
import java.lang.reflect.Method

class CacheKeyGenerator : KeyGenerator {
    override fun generate(
        target: Any,
        method: Method,
        vararg params: Any?
    ): Any {
        val keys = mutableListOf<Any?>()
        method.parameters.forEachIndexed { index, parameter ->
            if (!parameter.isAnnotationPresent(NotCacheKey::class.java)) {
                keys.add(params[index])
            }
        }
        return buildKeyString(keys)
    }

    fun generateString(vararg params: Any?): String {
        return buildKeyString(params.toList())
    }

    private fun buildKeyString(params: List<Any?>): String {
        return params.joinToString("-") { it?.toString() ?: "null" }
            .replace(" ", "")
    }
}