package com.example.ms_auth.services

import com.example.ms_auth.exceptions.NotFoundException
import com.example.ms_auth.models.User
import com.example.ms_auth.repositories.UserRepository
import com.example.ms_auth.utils.orderByToSort
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

interface IUserService {
    fun save(user: User): User
    fun findByEmail(email: String): User
    fun existsByEmail(email: String): Boolean
    fun findById(id: UUID): User
    fun findAll(page: Int = 0, size: Int = 10, orderBy: String?, search: String?): Page<User>
    fun deleteById(id: UUID)
}

@Service
class UserService(
    private val userRepository: UserRepository,
) : IUserService {
    override fun save(user: User): User {
        return userRepository.save(user)
    }

    override fun findByEmail(email: String): User {
        return userRepository.findByEmail(email) ?: throw NotFoundException()
    }

    override fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    override fun findById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("User not found") }
    }

    override fun findAll(
        page: Int,
        size: Int,
        orderBy: String?,
        search: String?
    ): Page<User> {
        val sort = orderByToSort(orderBy)
        val pageable = PageRequest.of(page, size, sort)
        return userRepository.findAllBySearch(search, pageable)
    }

    override fun deleteById(id: UUID) {
        userRepository.deleteById(id)
    }
}