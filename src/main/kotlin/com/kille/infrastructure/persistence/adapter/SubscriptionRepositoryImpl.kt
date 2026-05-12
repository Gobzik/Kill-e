package com.kille.infrastructure.persistence.adapter

import com.kille.domain.model.Subscription
import com.kille.domain.model.SubscriptionStatus
import com.kille.domain.repository.SubscriptionRepository
import com.kille.infrastructure.persistence.entity.SubscriptionEntity
import com.kille.infrastructure.persistence.repository.JpaSubscriptionRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SubscriptionRepositoryImpl(
    private val jpaSubscriptionRepository: JpaSubscriptionRepository
) : SubscriptionRepository {
    override fun save(subscription: Subscription): Subscription {
        val saved = jpaSubscriptionRepository.save(subscription.toEntity())
        return saved.toDomain()
    }

    override fun findActiveByUserId(userId: UUID): Subscription? =
        jpaSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)?.toDomain()

    override fun findByUserId(userId: UUID): List<Subscription> =
        jpaSubscriptionRepository.findAllByUserId(userId).map { it.toDomain() }

    private fun Subscription.toEntity(): SubscriptionEntity = SubscriptionEntity(
        id = id,
        userId = userId,
        plan = plan,
        status = status,
        startedAt = startedAt,
        expiresAt = expiresAt,
        createdAt = createdAt
    )

    private fun SubscriptionEntity.toDomain(): Subscription = Subscription(
        id = id,
        userId = userId,
        plan = plan,
        status = status,
        startedAt = startedAt,
        expiresAt = expiresAt,
        createdAt = createdAt
    )
}
