package com.kille.domain.model

import java.time.Instant
import java.util.UUID

enum class SubscriptionPlan {
    STANDARD,
    PREMIUM
}

enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELED
}

data class Subscription(
    val id: UUID? = null,
    val userId: UUID,
    val plan: SubscriptionPlan,
    val status: SubscriptionStatus,
    val startedAt: Instant,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now()
)
