package com.kille.application.service

import com.kille.application.port.output.RoleAssignmentPort
import com.kille.domain.model.Role
import com.kille.domain.model.Subscription
import com.kille.domain.model.SubscriptionPlan
import com.kille.domain.model.SubscriptionStatus
import com.kille.domain.model.User
import com.kille.domain.repository.SubscriptionRepository
import com.kille.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class PurchaseSubscriptionUseCaseTest {
    @Test
    fun `purchase assigns subscription role using port`() {
        val userId = UUID.randomUUID()
        val userRepository = InMemoryUserRepository(
            mutableMapOf(
                userId to User(
                    id = userId,
                    username = "u",
                    password = "p",
                    email = "u@mail.com",
                    roles = mutableListOf(Role(name = "ROLE_USER"))
                )
            )
        )
        val subscriptionRepository = InMemorySubscriptionRepository()
        val rolePort = CapturingRoleAssignmentPort()
        val useCase = PurchaseSubscriptionUseCase(userRepository, subscriptionRepository, rolePort)

        val result = useCase.purchase(userId, SubscriptionPlan.PREMIUM, 30)

        assertEquals(SubscriptionPlan.PREMIUM, result.plan)
        assertEquals(userId, rolePort.assignedUserId)
        assertEquals("PREMIUM", rolePort.assignedPlan)
    }

    @Test
    fun `expireSubscription clears subscription roles`() {
        val userId = UUID.randomUUID()
        val userRepository = InMemoryUserRepository(
            mutableMapOf(
                userId to User(id = userId, username = "u", password = "p", email = "u@mail.com")
            )
        )
        val subscriptionRepository = InMemorySubscriptionRepository()
        val active = subscriptionRepository.save(
            Subscription(
                userId = userId,
                plan = SubscriptionPlan.STANDARD,
                status = SubscriptionStatus.ACTIVE,
                startedAt = Instant.now(),
                expiresAt = Instant.now().plusSeconds(60)
            )
        )
        assertEquals(SubscriptionStatus.ACTIVE, active.status)
        val rolePort = CapturingRoleAssignmentPort()
        val useCase = PurchaseSubscriptionUseCase(userRepository, subscriptionRepository, rolePort)

        useCase.expireSubscription(userId)

        assertEquals(userId, rolePort.clearedUserId)
        assertTrue(subscriptionRepository.findByUserId(userId).any { it.status == SubscriptionStatus.EXPIRED })
    }
}

private class CapturingRoleAssignmentPort : RoleAssignmentPort {
    var assignedUserId: UUID? = null
    var assignedPlan: String? = null
    var clearedUserId: UUID? = null

    override fun assignSubscriptionRole(userId: UUID, plan: String) {
        assignedUserId = userId
        assignedPlan = plan
    }

    override fun clearSubscriptionRoles(userId: UUID) {
        clearedUserId = userId
    }
}

private class InMemorySubscriptionRepository : SubscriptionRepository {
    private val data = mutableListOf<Subscription>()

    override fun save(subscription: Subscription): Subscription {
        val withId = if (subscription.id == null) subscription.copy(id = UUID.randomUUID()) else subscription
        data.removeIf { it.id == withId.id }
        data.add(withId)
        return withId
    }

    override fun findActiveByUserId(userId: UUID): Subscription? =
        data.firstOrNull { it.userId == userId && it.status == SubscriptionStatus.ACTIVE }

    override fun findByUserId(userId: UUID): List<Subscription> = data.filter { it.userId == userId }
}

private class InMemoryUserRepository(
    private val data: MutableMap<UUID, User> = mutableMapOf()
) : UserRepository {
    override fun save(user: User): User {
        val id = user.id ?: UUID.randomUUID()
        val withId = user.copy(id = id)
        data[id] = withId
        return withId
    }

    override fun findById(id: UUID): User? = data[id]

    override fun findByUsername(username: String): User? = data.values.firstOrNull { it.username == username }

    override fun findByEmail(email: String): User? = data.values.firstOrNull { it.email == email }

    override fun existsAny(): Boolean = data.isNotEmpty()

    override fun delete(user: User) {
        user.id?.let { data.remove(it) }
    }
}
