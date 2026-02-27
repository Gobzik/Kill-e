package com.audiobook.domain.model

import com.audiobook.domain.exception.DomainException
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
data class ExampleEntity private constructor(
    /**
     * Уникальный идентификатор сущности.
     * Value Object - инкапсулирует логику ID.
     */
    val id: ExampleEntityId,

    /**
     * Название задачи.
     * Инкапсулировано - изменяется только через методы.
     */
    private var _title: String,

    /**
     * Описание задачи.
     * Опциональное поле.
     */
    private var _description: String?,

    /**
     * Статус задачи.
     * Enum - ограниченный набор значений.
     */
    private var _status: TaskStatus,

    /**
     * Приоритет задачи.
     * Используется для сортировки и планирования.
     */
    private var _priority: Priority,

    /**
     * Оценка сложности (в условных единицах, например story points).
     * Должна быть положительной.
     */
    private var _estimatedEffort: Int,

    /**
     * Фактически затраченное время.
     * Увеличивается через логирование работы.
     */
    private var _actualEffort: Int = 0,

    /**
     * Дата создания (audit field).
     * Неизменяемая после создания.
     */
    val createdAt: LocalDateTime,

    /**
     * Дата последнего обновления (audit field).
     * Автоматически обновляется при изменениях.
     */
    private var _updatedAt: LocalDateTime,

    /**
     * Дата завершения.
     * Устанавливается при переходе в статус COMPLETED.
     */
    private var _completedAt: LocalDateTime? = null
) {

    // ========== Публичные read-only свойства (геттеры) ==========

    val title: String get() = _title
    val description: String? get() = _description
    val status: TaskStatus get() = _status
    val priority: Priority get() = _priority
    val estimatedEffort: Int get() = _estimatedEffort
    val actualEffort: Int get() = _actualEffort
    val updatedAt: LocalDateTime get() = _updatedAt
    val completedAt: LocalDateTime? get() = _completedAt

    /**
     * Вычисляемое свойство: прогресс выполнения в процентах.
     * Демонстрирует, что сущность может содержать вычисления.
     */
    val progressPercentage: Int get() {
        if (_estimatedEffort == 0) return 0
        val progress = (_actualEffort * 100) / _estimatedEffort
        return minOf(progress, 100) // Максимум 100%
    }

    /**
     * Вычисляемое свойство: задача просрочена?
     */
    val isOverEstimated: Boolean get() = _actualEffort > _estimatedEffort

    /**
     * Вычисляемое свойство: задача завершена?
     */
    val isCompleted: Boolean get() = _status == TaskStatus.COMPLETED

    companion object {
        /**
         * Фабричный метод для создания новой задачи.
         *
         * Гарантирует, что задача создаётся только в валидном состоянии.
         * Это единственный способ создать новую ExampleEntity.
         *
         * @param title Название задачи
         * @param description Описание (опционально)
         * @param priority Приоритет
         * @param estimatedEffort Оценка сложности
         * @return Новая задача в статусе TODO
         * @throws DomainException если данные невалидны
         */
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

        private fun validateTitle(title: String) {
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
     * Бизнес-метод: логирование затраченного времени.
     *
     * Увеличивает actualEffort на указанное количество.
     *
     * @param effort Затраченное время (должно быть положительным)
     * @return Обновлённая задача
     * @throws DomainException если effort невалиден или задача не в работе
     */
    fun logWork(effort: Int): ExampleEntity {
        if (effort <= 0) {
            throw DomainException("Effort must be positive")
        }

        // Бизнес-правило: можно логировать время только для задачи в работе
        if (_status != TaskStatus.IN_PROGRESS) {
            throw DomainException("Can only log work for task in progress")
        }

        return copy(
            _actualEffort = _actualEffort + effort,
            _updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Бизнес-метод: завершение задачи.
     *
     * Бизнес-правило: можно завершить только задачу в работе.
     */
    fun complete(): ExampleEntity {
        // Бизнес-правило: можно завершить только задачу в работе
        if (_status != TaskStatus.IN_PROGRESS) {
            throw DomainException("Can only complete task that is in progress")
        }

        val now = LocalDateTime.now()

        return copy(
            _status = TaskStatus.COMPLETED,
            _completedAt = now,
            _updatedAt = now
        )
    }

    /**
     * Бизнес-метод: отмена задачи.
     */
    fun cancel(): ExampleEntity {
        // Бизнес-правило: нельзя отменить уже завершённую задачу
        if (_status == TaskStatus.COMPLETED) {
            throw DomainException("Cannot cancel completed task")
        }

        return copy(
            _status = TaskStatus.CANCELLED,
            _updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Бизнес-метод: возврат задачи в работу (reopening).
     *
     * Бизнес-правило: можно вернуть только завершённую или отменённую задачу.
     */
    fun reopen(): ExampleEntity {
        // Бизнес-правило: можно вернуть только завершённую или отменённую
        if (_status != TaskStatus.COMPLETED && _status != TaskStatus.CANCELLED) {
            throw DomainException("Can only reopen completed or cancelled task")
        }

        return copy(
            _status = TaskStatus.IN_PROGRESS,
            _completedAt = null,
            _updatedAt = LocalDateTime.now()
        )
    }

    // ========== Бизнес-правила (запросы) ==========

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
