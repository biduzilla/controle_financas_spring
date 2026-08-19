package com.example.ms_category.configs

import com.example.ms_category.security.AuthenticatedUser
import org.hibernate.cfg.AvailableSettings
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

@Component
class TenantIdentifierResolver : CurrentTenantIdentifierResolver<UUID>, HibernatePropertiesCustomizer {
    private val tenantHolder = ThreadLocal<UUID>()

    fun setUserId(userId: UUID) {
        tenantHolder.set(userId)
    }

    override fun resolveCurrentTenantIdentifier(): UUID? {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication?.principal

        if (principal is AuthenticatedUser) {
            return principal.userId
        }

        val tenant = tenantHolder.get()
        if (tenant != null) {
            return tenant
        }

        return UUID.fromString("00000000-0000-0000-0000-000000000000")
    }

    override fun validateExistingCurrentSessions(): Boolean = true

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties[AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER] = this
    }

    fun clear() {
        tenantHolder.remove()
    }
}