package com.kille.infrastructure.persistence.adapter

import com.kille.domain.model.Role
import com.kille.domain.model.User
import com.kille.domain.repository.RoleRepository
import com.kille.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.UUID

class RoleAssignmentAdapterTest {
    @Test
    fun `assignSubscriptionRole sets standard role`() {
        val userId = UUID.randomUUID()
        val userRepository = TestUserRepository(
            mutableMapOf(
                userId to User(
                    id = userId,
                    username = "user",
                    password = "pwd",
                    email = "u@mail.com",
                    roles = mutableListOf(Role(name = "ROLE_USER"))
                )
            )
        )
        val roleRepository = TestRoleRepository()
        val adapter = RoleAssignmentAdapter(userRepository, roleRepository)

        adapter.assignSubscriptionRole(userId, "STANDARD")

        val saved = userRepository.findById(userId)!!
        assertEquals(setOf("ROLE_USER", "ROLE_STANDARD"), saved.roles.map { it.name }.toSet())
    }

    @Test
    fun `assignSubscriptionRole premium removes standard`() {
        val userId = UUID.randomUUID()
        val userRepository = TestUserRepository(
            mutableMapOf(
                userId to User(
                    id = userId,
                    username = "user",
                    password = "pwd",
                    email = "u@mail.com",
                    roles = mutableListOf(Role(name = "ROLE_USER"), Role(name = "ROLE_STANDARD"))
                )
            )
        )
        val roleRepository = TestRoleRepository()
        val adapter = RoleAssignmentAdapter(userRepository, roleRepository)

        adapter.assignSubscriptionRole(userId, "PREMIUM")

        val roles = userRepository.findById(userId)!!.roles.map { it.name }
        assertEquals(setOf("ROLE_USER", "ROLE_PREMIUM"), roles.toSet())
        assertFalse(roles.contains("ROLE_STANDARD"))
    }

    @Test
    fun `clearSubscriptionRoles removes only subscription roles`() {
        val userId = UUID.randomUUID()
        val userRepository = TestUserRepository(
            mutableMapOf(
                userId to User(
                    id = userId,
                    username = "user",
                    password = "pwd",
                    email = "u@mail.com",
                    roles = mutableListOf(
                        Role(name = "ROLE_USER"),
                        Role(name = "ROLE_STANDARD"),
                        Role(name = "ROLE_PREMIUM")
                    )
                )
            )
        )
        val roleRepository = TestRoleRepository()
        val adapter = RoleAssignmentAdapter(userRepository, roleRepository)

        adapter.clearSubscriptionRoles(userId)

        val roles = userRepository.findById(userId)!!.roles.map { it.name }
        assertEquals(listOf("ROLE_USER"), roles)
    }
}

private class TestRoleRepository : RoleRepository {
    private val roles = mutableMapOf(
        "ROLE_USER" to Role(name = "ROLE_USER"),
        "ROLE_STANDARD" to Role(name = "ROLE_STANDARD"),
        "ROLE_PREMIUM" to Role(name = "ROLE_PREMIUM"),
        "ROLE_ADMIN" to Role(name = "ROLE_ADMIN")
    )

    override fun save(role: Role): Role {
        roles[role.name] = role
        return role
    }

    override fun findById(id: UUID): Role? = roles.values.firstOrNull { it.id == id }

    override fun findByName(name: String): Role? = roles[name]
}

private class TestUserRepository(
    private val users: MutableMap<UUID, User> = mutableMapOf()
) : UserRepository {
    override fun save(user: User): User {
        val id = user.id ?: UUID.randomUUID()
        val withId = user.copy(id = id)
        users[id] = withId
        return withId
    }

    override fun findById(id: UUID): User? = users[id]

    override fun findByUsername(username: String): User? = users.values.firstOrNull { it.username == username }

    override fun findByEmail(email: String): User? = users.values.firstOrNull { it.email == email }

    override fun existsAny(): Boolean = users.isNotEmpty()

    override fun delete(user: User) {
        user.id?.let { users.remove(it) }
    }
}
