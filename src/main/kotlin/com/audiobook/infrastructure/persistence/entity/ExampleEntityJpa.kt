package com.yourapp.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA Entity для ExampleEntity.
 *
 * Это персистентная модель, НЕ доменная!
 * Содержит JPA аннотации и технические детали БД.
 *
 * Преобразование Domain <-> Entity происходит в Adapter.
 */
@Entity
@Table(name = "example_entities")
data class ExampleEntityJpa(

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "description", length = 1000)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: TaskStatusJpa,

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    var priority: PriorityJpa,

    @Column(name = "estimated_effort", nullable = false)
    var estimatedEffort: Int,

    @Column(name = "actual_effort", nullable = false)
    var actualEffort: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null
)

/**
 * Enum для статуса на уровне БД.
 */
enum class TaskStatusJpa {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

/**
 * Enum для приоритета на уровне БД.
 */
enum class PriorityJpa {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
