package com.kille.application.service

import com.kille.application.port.output.RoleAssignmentPort
import com.kille.domain.model.Subscription
import com.kille.domain.model.SubscriptionPlan
import com.kille.domain.model.SubscriptionStatus
import com.kille.domain.repository.SubscriptionRepository
import com.kille.domain.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class PurchaseSubscriptionUseCase(
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val roleAssignmentPort: RoleAssignmentPort
) {
    fun purchase(userId: UUID, plan: SubscriptionPlan, durationDays: Long): Subscription {
        require(durationDays > 0) { "durationDays must be greater than 0" }
        userRepository.findById(userId) ?: throw NoSuchElementException("User not found")

        val now = Instant.now()
        val current = subscriptionRepository.findActiveByUserId(userId)

        if (current != null) {
            subscriptionRepository.save(current.copy(status = SubscriptionStatus.EXPIRED))
        }

        val saved = subscriptionRepository.save(
            Subscription(
                userId = userId,
                plan = plan,
                status = SubscriptionStatus.ACTIVE,
                startedAt = now,
                expiresAt = now.plus(durationDays, ChronoUnit.DAYS)
            )
        )

        val normalizedPlan = if (plan == SubscriptionPlan.PREMIUM) "PREMIUM" else "STANDARD"
        roleAssignmentPort.assignSubscriptionRole(userId, normalizedPlan)
        return saved
    }

    fun expireSubscription(userId: UUID) {
        val current = subscriptionRepository.findActiveByUserId(userId) ?: return
        subscriptionRepository.save(current.copy(status = SubscriptionStatus.EXPIRED))
        roleAssignmentPort.clearSubscriptionRoles(userId)
    }
}
