package com.example.ms_auth.services

import com.example.ms_auth.dto.CreateUserRequest
import com.example.ms_auth.dto.UpdateUserRequest
import com.example.ms_auth.exceptions.BadRequestException
import com.example.ms_auth.exceptions.NotFoundException
import com.example.ms_auth.models.User
import com.example.ms_auth.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import java.util.UUID.randomUUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var service: UserService

    private fun createFakeUser(
        id: UUID = randomUUID(),
        email: String = "test@email.com",
        passwordHash: String = "hash123",
        name: String = "Test User"
    ) = User(
        id = id,
        email = email,
        passwordHash = passwordHash,
        name = name
    )

    @Test
    fun `signUp should save user when email is new and password is valid`() {
        val request = CreateUserRequest(
            email = "new@email.com",
            password = "Password1",
            name = "New User"
        )

        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password)).thenReturn("hashedPassword1")

        val savedUser = createFakeUser(
            email = request.email,
            passwordHash = "hashedPassword1",
            name = request.name
        )
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)

        val result = service.signUp(request)
        assertEquals(savedUser, result)
        verify(userRepository).existsByEmail(request.email)
        verify(passwordEncoder).encode(request.password)
        verify(userRepository).save(argThat { u ->
            u.email == request.email &&
                    u.passwordHash == "hashedPassword1" &&
                    u.name == request.name
        })
    }

    @Test
    fun `signUp should throw BadRequestException when email already exists`() {
        val request = CreateUserRequest(
            email = "duplicate@email.com",
            password = "Password1",
            name = "Some Name"
        )
        whenever(userRepository.existsByEmail(request.email)).thenReturn(true)

        val exception = assertThrows<BadRequestException> {
            service.signUp(request)
        }
        assertEquals("Email already exists", exception.message)

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `signUp should throw BadRequestException when password is too short`() {
        val request = CreateUserRequest(
            email = "ok@email.com",
            password = "Ab1",
            name = "Name"
        )
        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)

        val exception = assertThrows<BadRequestException> {
            service.signUp(request)
        }
        assertTrue(exception.message!!.contains("8 digits"))

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `signUp should throw BadRequestException when password has no letter`() {
        val request = CreateUserRequest(
            email = "ok@email.com",
            password = "12345678",
            name = "Name"
        )
        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)

        val exception = assertThrows<BadRequestException> {
            service.signUp(request)
        }
        assertTrue(exception.message!!.contains("one letter"))

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `signUp should throw BadRequestException when password has no digit`() {
        val request = CreateUserRequest(
            email = "ok@email.com",
            password = "Password",
            name = "Name"
        )
        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)

        val exception = assertThrows<BadRequestException> {
            service.signUp(request)
        }
        assertTrue(exception.message!!.contains("one digit"))

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `update should update user fields when valid data is provided`() {
        val id = randomUUID()
        val existingUser = createFakeUser(id = id, email = "old@email.com", name = "Old Name")
        val request = UpdateUserRequest(
            email = "new@email.com",
            password = "Password1",
            name = "New User"
        )

        whenever(userRepository.findById(id)).thenReturn(Optional.of(existingUser))
        whenever(userRepository.existsByEmail(request.email!!)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password!!)).thenReturn("hashedNewPassword")
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] as User }

        val result = service.update(id, request)

        // O usuário retornado deve ter os novos dados
        assertEquals(request.email, result.email)
        assertEquals("hashedNewPassword", result.passwordHash)
        assertEquals(request.name, result.name)

        // Verifica se save foi chamado com os campos atualizados
        verify(userRepository).save(argThat { user ->
            user.email == request.email &&
                    user.passwordHash == "hashedNewPassword" &&
                    user.name == request.name
        })
        verify(passwordEncoder).encode(request.password!!)
    }

    @Test
    fun `update should throw BadRequestException when email already exists for another user`() {
        val id = randomUUID()
        val existingUser = createFakeUser(id = id, email = "user@email.com")
        val request = UpdateUserRequest(
            email = "duplicate@email.com",
            password = null,
            name = null
        )

        whenever(userRepository.findById(id)).thenReturn(Optional.of(existingUser))
        whenever(userRepository.existsByEmail(request.email!!)).thenReturn(true)

        val exception = assertThrows<BadRequestException> {
            service.update(id, request)
        }
        assertEquals("Email already exists", exception.message)

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `update should throw BadRequestException when password is too short`() {
        val id = randomUUID()
        val existingUser = createFakeUser(id = id, email = "user@email.com")
        val request = UpdateUserRequest(
            email = null,          // não altera e-mail
            password = "Ab1",      // senha curta
            name = null
        )

        whenever(userRepository.findById(id)).thenReturn(Optional.of(existingUser))

        val exception = assertThrows<BadRequestException> {
            service.update(id, request)
        }
        assertTrue(exception.message!!.contains("8 digits"))

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `update should throw BadRequestException when password has no letter`() {
        val id = randomUUID()
        val existingUser = createFakeUser(id = id, email = "user@email.com")
        val request = UpdateUserRequest(email = null, password = "12345678", name = null)

        whenever(userRepository.findById(id)).thenReturn(Optional.of(existingUser))

        val exception = assertThrows<BadRequestException> {
            service.update(id, request)
        }
        assertTrue(exception.message!!.contains("one letter"))

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `update should throw BadRequestException when password has no digit`() {
        val id = randomUUID()
        val existingUser = createFakeUser(id = id, email = "user@email.com")
        val request = UpdateUserRequest(email = null, password = "Password", name = null)

        whenever(userRepository.findById(id)).thenReturn(Optional.of(existingUser))

        val exception = assertThrows<BadRequestException> {
            service.update(id, request)
        }
        assertTrue(exception.message!!.contains("one digit"))

        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `save should return user when repository saves successfully`() {
        val fakeUser = createFakeUser()
        whenever(userRepository.save(fakeUser)).thenReturn(fakeUser)

        val result = service.save(fakeUser)

        assertEquals(fakeUser, result)
        verify(userRepository).save(fakeUser)
    }

    @Test
    fun `findByEmail should return user when email exists`() {
        val email = "test@email.com"
        val fakeUser = createFakeUser(email = email)
        whenever(userRepository.findByEmail(email)).thenReturn(fakeUser)

        val result = service.findByEmail(email)

        assertEquals(fakeUser, result)
        verify(userRepository).findByEmail(email)
    }

    @Test
    fun `findByEmail should throw NotFoundException when email does not exist`() {
        val email = "notfound@email.com"
        whenever(userRepository.findByEmail(email)).thenReturn(null)

        assertThrows<NotFoundException> {
            service.findByEmail(email)
        }

        verify(userRepository).findByEmail(email)
    }

    @Test
    fun `existsByEmail should return true when user exists`() {
        val email = "test@email.com"
        whenever(userRepository.existsByEmail(email)).thenReturn(true)
        val result = service.existsByEmail(email)
        assertTrue(result)
    }

    @Test
    fun `existsByEmail should return false when user does not exist`() {
        val email = "test@email.com"
        whenever(userRepository.existsByEmail(email)).thenReturn(false)

        val result = service.existsByEmail(email)

        assertFalse(result)
    }

    @Test
    fun `findById should return user when id exists`() {
        val id = randomUUID()
        val fakeUser = createFakeUser(id = id)
        whenever(userRepository.findById(id)).thenReturn(Optional.of(fakeUser))

        val result = service.findById(id)

        assertEquals(fakeUser, result)
    }

    @Test
    fun `findById should throw NotFoundException with message when id does not exist`() {
        val id = randomUUID()
        whenever(userRepository.findById(id)).thenReturn(java.util.Optional.empty())

        val exception = assertThrows<NotFoundException> {
            service.findById(id)
        }

        assertEquals("User not found", exception.message)
    }

    @Test
    fun `findAll should return paged users`() {
        val fakeUser = createFakeUser()
        val expectedPage: Page<User> = PageImpl(listOf(fakeUser))

        whenever(userRepository.findAllBySearch(any(), any())).thenReturn(expectedPage)

        val result = service.findAll(page = 0, size = 10, orderBy = "name", search = "test")

        assertEquals(1, result.totalElements)
        assertEquals(fakeUser, result.content[0])
        verify(userRepository).findAllBySearch(any(), any())
    }

    @Test
    fun `deleteById should call repository deleteById`() {
        val id = randomUUID()

        doNothing().whenever(userRepository).deleteById(id)
        service.deleteById(id)

        verify(userRepository, times(1)).deleteById(id)
    }
}