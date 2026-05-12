package com.kille.infrastructure.persistence.repository

import com.kille.domain.model.SubscriptionStatus
import com.kille.infrastructure.persistence.entity.SubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaSubscriptionRepository : JpaRepository<SubscriptionEntity, UUID> {
    fun findByUserIdAndStatus(userId: UUID, status: SubscriptionStatus): SubscriptionEntity?
    fun findAllByUserId(userId: UUID): List<SubscriptionEntity>
}
