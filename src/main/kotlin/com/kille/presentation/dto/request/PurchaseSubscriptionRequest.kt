package com.kille.presentation.dto.request

import com.kille.domain.model.SubscriptionPlan
import java.util.UUID

data class PurchaseSubscriptionRequest(
    val userId: UUID,
    val plan: SubscriptionPlan,
    val durationDays: Long
)
