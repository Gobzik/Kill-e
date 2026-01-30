package com.yourapp.infrastructure.persistence.adapter

import com.yourapp.domain.exception.EntityNotFoundException
import com.yourapp.domain.model.*
import com.yourapp.domain.repository.ExampleRepository
import com.yourapp.infrastructure.persistence.entity.ExampleEntityJpa
import com.yourapp.infrastructure.persistence.entity.PriorityJpa
import com.yourapp.infrastructure.persistence.entity.TaskStatusJpa
import com.yourapp.infrastructure.persistence.repository.ExampleJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Adapter для ExampleRepository.
 *
 * Реализует доменный интерфейс используя JPA.
 * Выполняет преобразование Domain Model <-> JPA Entity.
 *
 * Паттерн: Adapter (Hexagonal Architecture)
 *
 * Принцип: Dependency Inversion
 * - Infrastructure зависит от Domain
 * - Domain НЕ зависит от Infrastructure
 */
@Component
@Transactional
class ExampleRepositoryAdapter(
    private val jpaRepository: ExampleJpaRepository
) : ExampleRepository {

    override fun save(entity: ExampleEntity): ExampleEntity {
        val jpaEntity = toJpaEntity(entity)
        val saved = jpaRepository.save(jpaEntity)
        return toDomain(saved)
    }

    @Transactional(readOnly = true)
    override fun findById(id: ExampleEntityId): ExampleEntity? {
        val uuid = UUID.fromString(id.toString())
        return jpaRepository.findById(uuid)
            .map { toDomain(it) }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<ExampleEntity> {
        return jpaRepository.findAll()
            .map { toDomain(it) }
    }

    @Transactional(readOnly = true)
    override fun findByStatus(status: TaskStatus): List<ExampleEntity> {
        val jpaStatus = toJpaStatus(status)
        return jpaRepository.findByStatus(jpaStatus)
            .map { toDomain(it) }
    }

    @Transactional(readOnly = true)
    override fun findByPriority(priority: Priority): List<ExampleEntity> {
        val jpaPriority = toJpaPriority(priority)
        return jpaRepository.findByPriority(jpaPriority)
            .map { toDomain(it) }
    }

    override fun deleteById(id: ExampleEntityId) {
        val uuid = UUID.fromString(id.toString())
        if (!jpaRepository.existsById(uuid)) {
            throw EntityNotFoundException("ExampleEntity", id)
        }
        jpaRepository.deleteById(uuid)
    }

    @Transactional(readOnly = true)
    override fun existsById(id: ExampleEntityId): Boolean {
        val uuid = UUID.fromString(id.toString())
        return jpaRepository.existsById(uuid)
    }

    // ========== Mapper Methods: Domain <-> JPA Entity ==========

    /**
     * Преобразование Domain Model -> JPA Entity.
     */
    private fun toJpaEntity(domain: ExampleEntity): ExampleEntityJpa {
        return ExampleEntityJpa(
            id = UUID.fromString(domain.id.toString()),
            title = domain.title,
            description = domain.description,
            status = toJpaStatus(domain.status),
            priority = toJpaPriority(domain.priority),
            estimatedEffort = domain.estimatedEffort,
            actualEffort = domain.actualEffort,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            completedAt = domain.completedAt
        )
    }

    /**
     * Преобразование JPA Entity -> Domain Model.
     */
    private fun toDomain(jpa: ExampleEntityJpa): ExampleEntity {
        return ExampleEntity.restore(
            id = ExampleEntityId.fromString(jpa.id.toString()),
            title = jpa.title,
            description = jpa.description,
            status = toDomainStatus(jpa.status),
            priority = toDomainPriority(jpa.priority),
            estimatedEffort = jpa.estimatedEffort,
            actualEffort = jpa.actualEffort,
            createdAt = jpa.createdAt,
            updatedAt = jpa.updatedAt,
            completedAt = jpa.completedAt
        )
    }

    /**
     * Маппинг статусов: Domain -> JPA.
     */
    private fun toJpaStatus(status: TaskStatus): TaskStatusJpa {
        return when (status) {
            TaskStatus.TODO -> TaskStatusJpa.TODO
            TaskStatus.IN_PROGRESS -> TaskStatusJpa.IN_PROGRESS
            TaskStatus.COMPLETED -> TaskStatusJpa.COMPLETED
            TaskStatus.CANCELLED -> TaskStatusJpa.CANCELLED
        }
    }

    /**
     * Маппинг статусов: JPA -> Domain.
     */
    private fun toDomainStatus(status: TaskStatusJpa): TaskStatus {
        return when (status) {
            TaskStatusJpa.TODO -> TaskStatus.TODO
            TaskStatusJpa.IN_PROGRESS -> TaskStatus.IN_PROGRESS
            TaskStatusJpa.COMPLETED -> TaskStatus.COMPLETED
            TaskStatusJpa.CANCELLED -> TaskStatus.CANCELLED
        }
    }

    /**
     * Маппинг приоритетов: Domain -> JPA.
     */
    private fun toJpaPriority(priority: Priority): PriorityJpa {
        return when (priority) {
            Priority.LOW -> PriorityJpa.LOW
            Priority.MEDIUM -> PriorityJpa.MEDIUM
            Priority.HIGH -> PriorityJpa.HIGH
            Priority.CRITICAL -> PriorityJpa.CRITICAL
        }
    }

    /**
     * Маппинг приоритетов: JPA -> Domain.
     */
    private fun toDomainPriority(priority: PriorityJpa): Priority {
        return when (priority) {
            PriorityJpa.LOW -> Priority.LOW
            PriorityJpa.MEDIUM -> Priority.MEDIUM
            PriorityJpa.HIGH -> Priority.HIGH
            PriorityJpa.CRITICAL -> Priority.CRITICAL
        }
    }
}
