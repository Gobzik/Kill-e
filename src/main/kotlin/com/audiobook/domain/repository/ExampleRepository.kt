package com.audiobook.domain.repository

import com.audiobook.domain.model.ExampleEntity
import com.audiobook.domain.model.ExampleEntityId
import com.audiobook.domain.model.Priority
import com.audiobook.domain.model.TaskStatus

/**
 * Интерфейс репозитория для ExampleEntity.
 *
 * Domain Layer определяет контракт (интерфейс),
 * Infrastructure Layer предоставляет реализацию.
 *
 * Принцип: Dependency Inversion Principle (DIP)
 * - Domain не зависит от Infrastructure
 * - Infrastructure зависит от Domain
 */
interface ExampleRepository {

    /**
     * Сохранение или обновление сущности.
     */
    fun save(entity: ExampleEntity): ExampleEntity

    /**
     * Поиск по ID.
     */
    fun findById(id: ExampleEntityId): ExampleEntity?

    /**
     * Получение всех сущностей.
     */
    fun findAll(): List<ExampleEntity>

    /**
     * Поиск по статусу.
     */
    fun findByStatus(status: TaskStatus): List<ExampleEntity>

    /**
     * Поиск по приоритету.
     */
    fun findByPriority(priority: Priority): List<ExampleEntity>

    /**
     * Удаление по ID.
     */
    fun deleteById(id: ExampleEntityId)

    /**
     * Проверка существования.
     */
    fun existsById(id: ExampleEntityId): Boolean
}
