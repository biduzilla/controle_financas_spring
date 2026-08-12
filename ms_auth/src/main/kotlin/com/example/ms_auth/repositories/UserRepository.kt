package com.example.ms_auth.repositories

import com.example.ms_auth.models.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    @Query("select u from User u where u.email = :email")
    fun findByEmail(@Param("email") email: String): User?

    @Query(
        """
    SELECT CASE WHEN EXISTS (
        SELECT 1 FROM User u WHERE u.email = :email
    ) THEN TRUE ELSE FALSE END
"""
    )
    fun existsByEmail(@Param("email") email: String): Boolean

    @Query(
        """
    SELECT u
    FROM User u
    WHERE (
        :search IS NULL
        OR u.name LIKE CONCAT('%', :search, '%')
        OR u.email LIKE CONCAT('%', :search, '%')
    )
"""
    )
    fun findAllBySearch(@Param("search") search: String?, pageable: Pageable): Page<User>
}