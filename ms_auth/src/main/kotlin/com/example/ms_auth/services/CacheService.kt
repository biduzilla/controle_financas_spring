package com.example.ms_auth.services

import com.example.ms_auth.utils.CacheKeyGenerator
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CacheService(
    private val cacheManager: CacheManager,
    private val keyGenerator: KeyGenerator,
    private val stringRedisTemplate: StringRedisTemplate
) {
    fun clearCache(cacheName: String) {
        cacheManager.getCache(cacheName)?.clear()
    }

    fun evict(cacheName: String, vararg keys: Any?) {
        val key = (keyGenerator as CacheKeyGenerator).generateString(*keys)
        cacheManager.getCache(cacheName)?.evict(key)
    }

    fun put(cacheName: String, key: Any, value: Any) {
        cacheManager.getCache(cacheName)?.put(key, value)
    }

    fun get(cacheName: String, key: Any): Any? {
        return cacheManager.getCache(cacheName)?.get(key)?.get()
    }

    fun clearAllCaches() {
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
    }

    fun acquireLock(lockKey: String, lockValue: String, ttl: Duration = Duration.ofSeconds(30)): Boolean {
        return stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, ttl) ?: false
    }

    fun releaseLock(lockKey: String, lockValue: String) {
        val currentValue = stringRedisTemplate.opsForValue().get(lockKey)
        if (lockValue == currentValue) {
            stringRedisTemplate.delete(lockKey)
        }
    }
}