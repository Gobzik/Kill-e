package com.kille.domain.repository

import com.kille.domain.model.Subscription
import java.util.UUID

interface SubscriptionRepository {
    fun save(subscription: Subscription): Subscription
    fun findActiveByUserId(userId: UUID): Subscription?
    fun findByUserId(userId: UUID): List<Subscription>
}
