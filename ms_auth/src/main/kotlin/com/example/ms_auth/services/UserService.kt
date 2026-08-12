package com.example.ms_auth.services

import com.example.ms_auth.dto.CreateUserRequest
import com.example.ms_auth.dto.UpdateUserRequest
import com.example.ms_auth.exceptions.BadRequestException
import com.example.ms_auth.exceptions.NotFoundException
import com.example.ms_auth.models.User
import com.example.ms_auth.repositories.UserRepository
import com.example.ms_auth.utils.orderByToSort
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

interface IUserService {
    fun save(user: User): User
    fun signUp(req: CreateUserRequest): User
    fun update(id: UUID, req: UpdateUserRequest): User
    fun findByEmail(email: String): User
    fun existsByEmail(email: String): Boolean
    fun findById(id: UUID): User
    fun findAll(page: Int = 0, size: Int = 10, orderBy: String?, search: String?): Page<User>
    fun deleteById(id: UUID)
}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : IUserService {
    override fun save(user: User): User {
        return userRepository.save(user)
    }

    override fun signUp(req: CreateUserRequest): User {
        if (existsByEmail(req.email)) {
            throw BadRequestException("Email already exists")
        }

        verifyPassword(req.password)
        val user = User(
            email = req.email,
            passwordHash = passwordEncoder.encode(req.password).orEmpty(),
            name = req.name,
        )
        return userRepository.save(user)
    }

    override fun update(id: UUID, req: UpdateUserRequest): User {
        val user = findById(id)

        var isAltered = false
        if (!req.email.isNullOrBlank() && req.email != user.email) {
            if (existsByEmail(req.email)) {
                throw BadRequestException("Email already exists")
            }
            user.email = req.email
            isAltered = true
        }

        if (!req.password.isNullOrBlank()) {
            verifyPassword(req.password)
            user.passwordHash = passwordEncoder.encode(req.password).orEmpty()
            isAltered = true
        }

        if (!req.name.isNullOrBlank()) {
            user.name = req.name
            isAltered = true
        }

        return if (isAltered) userRepository.save(user) else user
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

    private fun verifyPassword(password: String) {
        if (password.length < 8) {
            throw BadRequestException("The password must contain at least 8 digits.")
        }

        if (!password.any { it.isLetter() }) {
            throw BadRequestException("The password must contain one letter.")
        }

        if (!password.any { it.isDigit() }) {
            throw BadRequestException("The password must contain one digit.")
        }
    }
}