package com.kille.infrastructure.persistence.entity

import com.kille.domain.model.SubscriptionPlan
import com.kille.domain.model.SubscriptionStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "subscriptions")
data class SubscriptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val plan: SubscriptionPlan,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: SubscriptionStatus,
    @Column(nullable = false)
    val startedAt: Instant,
    @Column(nullable = false)
    val expiresAt: Instant,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
