package com.kille.presentation.mapper

import com.kille.domain.model.Subscription
import com.kille.presentation.dto.response.SubscriptionResponse

fun Subscription.toResponse() = SubscriptionResponse(
    id = id ?: throw IllegalStateException("Subscription without id"),
    userId = userId,
    plan = plan,
    status = status,
    startedAt = startedAt,
    expiresAt = expiresAt,
    createdAt = createdAt
)
