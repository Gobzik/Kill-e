package com.yourapp.domain.model

import com.yourapp.domain.exception.DomainException
import java.time.LocalDateTime
import java.util.UUID

/**
 * ExampleEntity - демонстрационная богатая доменная сущность.
 *
 * Это пример реализации DDD (Domain-Driven Design) принципов:
 *
 * 1. Entity - имеет уникальный идентификатор и жизненный цикл
 * 2. Aggregate Root - управляет своими границами и инвариантами
 * 3. Богатая модель - содержит бизнес-логику, а не просто getter/setter
 * 4. Инкапсуляция - состояние изменяется только через методы
 * 5. Самовалидация - всегда находится в корректном состоянии
 * 6. Ubiquitous Language - термины из предметной области
 *
 * Представим, что это сущность "Задача" (Task) в системе управления проектами.
 */
 class Book(
    val _id: UUID,
    val _title: String,
    val _author: String,
    val _language: String,
    val _coverUrl: String?,
    private var _chapters: MutableList<Chapter>,
    val audio: Boolean,
    val text: Boolean,

) {

    // ========== Публичные read-only свойства (геттеры) ==========

    val title: String get() = _title
    val author: String get() = _author
    val language: String get() = _language
    val coverUrl: String? get() = _coverUrl
    addChapter(chapter: Chapter)
    getChapter(index: Int): Chapter
    chapterCount(): Int
    hasAudio(): Boolean
    hasText(): Boolean
    chapters(): List (immutable view)

        fun create(
            title: String,
            description: String? = null,
            priority: Priority = Priority.MEDIUM,
            estimatedEffort: Int
        ): ExampleEntity {
            // Валидация инвариантов при создании
            validateTitle(title)
            validateEstimatedEffort(estimatedEffort)

            val now = LocalDateTime.now()

            return ExampleEntity(
                id = ExampleEntityId.generate(),
                _title = title.trim(),
                _description = description?.trim(),
                _status = TaskStatus.TODO,
                _priority = priority,
                _estimatedEffort = estimatedEffort,
                _actualEffort = 0,
                createdAt = now,
                _updatedAt = now,
                _completedAt = null
            )
        }

        /**
         * Фабричный метод для восстановления сущности из БД.
         *
         * Используется только инфраструктурным слоем.
         * Не выполняет валидацию, т.к. предполагается что данные из БД валидны.
         */
        fun restore(
            id: ExampleEntityId,
            title: String,
            description: String?,
            status: TaskStatus,
            priority: Priority,
            estimatedEffort: Int,
            actualEffort: Int,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
            completedAt: LocalDateTime?
        ): ExampleEntity {
            return ExampleEntity(
                id = id,
                _title = title,
                _description = description,
                _status = status,
                _priority = priority,
                _estimatedEffort = estimatedEffort,
                _actualEffort = actualEffort,
                createdAt = createdAt,
                _updatedAt = updatedAt,
                _completedAt = completedAt
            )
        }

        // ========== Валидационные функции ==========
        fun addChapter(chapter: Chapter) {
            _chapters.add(chapter)
            ensureSorting()
        }
        private fun addChapter(chapter: Chapter) {
            if (title.isBlank()) {
                throw DomainException("Task title cannot be empty")
            }
            if (title.length > 200) {
                throw DomainException("Task title too long (max 200 characters)")
            }
        }

        private fun validateEstimatedEffort(effort: Int) {
            if (effort <= 0) {
                throw DomainException("Estimated effort must be positive")
            }
            if (effort > 100) {
                throw DomainException("Estimated effort too large (max 100)")
            }
        }
    }

    // ========== Бизнес-методы (команды) ==========

    /**
     * Бизнес-метод: изменение названия задачи.
     *
     * Инкапсулирует логику валидации и обновления.
     * Возвращает новый экземпляр (immutability).
     *
     * @param newTitle Новое название
     * @return Обновлённая задача
     * @throws DomainException если название невалидно
     */
    fun changeTitle(newTitle: String): ExampleEntity {
        validateTitle(newTitle)

        if (_title == newTitle.trim()) {
            return this // Оптимизация: если не изменилось - возвращаем this
        }

        return copy(
            _title = newTitle.trim(),
            _updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Бизнес-метод: изменение описания.
     */
    fun changeDescription(newDescription: String?): ExampleEntity {
        if (_description == newDescription?.trim()) {
            return this
        }

        return copy(
            _description = newDescription?.trim(),
            _updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Бизнес-метод: изменение приоритета.
     */
    fun changePriority(newPriority: Priority): ExampleEntity {
        if (_priority == newPriority) {
            return this
        }

        return copy(
            _priority = newPriority,
            _updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Бизнес-метод: переоценка сложности.
     *
     * Бизнес-правило: нельзя изменить оценку завершённой задачи.
     */
    fun reestimate(newEstimatedEffort: Int): ExampleEntity {
        validateEstimatedEffort(newEstimatedEffort)

        // Бизнес-правило: нельзя менять оценку завершённой задачи
        if (_status == TaskStatus.COMPLETED) {
            throw DomainException("Cannot reestimate completed task")
        }

        return copy(
            _estimatedEffort = newEstimatedEffort,
            _updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Бизнес-метод: начало работы над задачей.
     *
     * Бизнес-правило: можно начать только задачу в статусе TODO.
     */
    fun startWork(): ExampleEntity {
        // Бизнес-правило: можно начать только задачу в TODO
        if (_status != TaskStatus.TODO) {
            throw DomainException("Can only start task with status TODO")
        }

        return copy(
            _status = TaskStatus.IN_PROGRESS,
            _updatedAt = LocalDateTime.now()
        )
    }


    /**
     * Бизнес-правило: можно ли редактировать задачу?
     *
     * Используется в Use Case для проверки прав.
     */
    fun canBeEdited(): Boolean {
        return _status != TaskStatus.COMPLETED && _status != TaskStatus.CANCELLED
    }

    /**
     * Бизнес-правило: требует ли задача внимания?
     *
     * Например, высокий приоритет и не в работе.
     */
    fun requiresAttention(): Boolean {
        return _priority == Priority.HIGH && _status == TaskStatus.TODO
    }

    /**
     * Бизнес-правило: время выполнения в пределах оценки?
     */
    fun isWithinEstimate(): Boolean {
        return _actualEffort <= _estimatedEffort
    }

    /**
     * Получение отклонения от оценки.
     * Положительное значение = превышение, отрицательное = в пределах.
     */
    fun getEstimateDeviation(): Int {
        return _actualEffort - _estimatedEffort
    }
}

/**
 * Value Object - ExampleEntityId
 *
 * Инкапсулирует идентификатор сущности.
 *
 * Принципы Value Object:
 * - Неизменяемый (immutable)
 * - Самовалидирующийся
 * - Сравнивается по значению
 * - Не имеет собственного идентификатора
 */
@JvmInline
value class ExampleEntityId(val value: UUID) {
    init {
        // Валидация при создании
        require(value.toString().isNotBlank()) {
            "ExampleEntityId cannot be empty"
        }
    }

    companion object {
        /**
         * Генерация нового уникального ID.
         */
        fun generate(): ExampleEntityId = ExampleEntityId(UUID.randomUUID())

        /**
         * Создание ID из строки (для десериализации).
         *
         * @param value Строковое представление UUID
         * @return ExampleEntityId
         * @throws DomainException если формат невалиден
         */
        fun fromString(value: String): ExampleEntityId {
            return try {
                ExampleEntityId(UUID.fromString(value))
            } catch (e: IllegalArgumentException) {
                throw DomainException("Invalid ExampleEntityId format: $value", e)
            }
        }
    }

    override fun toString(): String = value.toString()
}

/**
 * Enum - Статус задачи.
 *
 * Ограниченный набор состояний в жизненном цикле задачи.
 */
enum class TaskStatus(val displayName: String) {
    TODO("К выполнению"),
    IN_PROGRESS("В работе"),
    COMPLETED("Завершено"),
    CANCELLED("Отменено");

    /**
     * Проверка, является ли статус финальным.
     */
    fun isFinal(): Boolean = this == COMPLETED || this == CANCELLED
}

/**
 * Enum - Приоритет задачи.
 *
 * Используется для сортировки и планирования.
 */
enum class Priority(val level: Int, val displayName: String) {
    LOW(1, "Низкий"),
    MEDIUM(2, "Средний"),
    HIGH(3, "Высокий"),
    CRITICAL(4, "Критический");

    /**
     * Сравнение приоритетов.
     */
    fun isHigherThan(other: Priority): Boolean = this.level > other.level

    companion object {
        /**
         * Получение приоритета по уровню.
         */
        fun fromLevel(level: Int): Priority {
            return entries.find { it.level == level }
                ?: throw DomainException("Invalid priority level: $level")
        }
    }
}