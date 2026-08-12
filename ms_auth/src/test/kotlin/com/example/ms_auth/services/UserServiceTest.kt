package com.example.ms_auth.services

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
import java.util.*
import java.util.UUID.randomUUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository
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