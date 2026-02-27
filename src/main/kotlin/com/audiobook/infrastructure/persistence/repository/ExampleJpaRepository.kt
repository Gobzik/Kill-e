package com.audiobook.infrastructure.persistence.repository

import com.audiobook.infrastructure.persistence.entity.ExampleEntityJpa
import com.audiobook.infrastructure.persistence.entity.PriorityJpa
import com.audiobook.infrastructure.persistence.entity.TaskStatusJpa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA Repository для ExampleEntityJpa.
 *
 * Автоматически реализуется Spring Data JPA.
 * Предоставляет CRUD операции и кастомные запросы.
 */
@Repository
interface ExampleJpaRepository : JpaRepository<ExampleEntityJpa, UUID> {

    /**
     * Поиск по статусу.
     */
    fun findByStatus(status: TaskStatusJpa): List<ExampleEntityJpa>

    /**
     * Поиск по приоритету.
     */
    fun findByPriority(priority: PriorityJpa): List<ExampleEntityJpa>

    /**
     * Поиск по статусу и приоритету.
     */
    fun findByStatusAndPriority(status: TaskStatusJpa, priority: PriorityJpa): List<ExampleEntityJpa>
}
