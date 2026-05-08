package com.kille.presentation.dto.response

import com.kille.domain.model.SubscriptionPlan
import com.kille.domain.model.SubscriptionStatus
import java.time.Instant
import java.util.UUID

data class SubscriptionResponse(
    val id: UUID,
    val userId: UUID,
    val plan: SubscriptionPlan,
    val status: SubscriptionStatus,
    val startedAt: Instant,
    val expiresAt: Instant,
    val createdAt: Instant
)
