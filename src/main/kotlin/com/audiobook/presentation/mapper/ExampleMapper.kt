package com.yourapp.presentation.mapper

import com.yourapp.application.dto.response.ExampleResponse
import com.yourapp.domain.model.ExampleEntity
import org.springframework.stereotype.Component

/**
 * Mapper для преобразования ExampleEntity <-> DTO.
 *
 * Паттерн: Data Mapper
 * Разделяет доменную модель от представления.
 */
@Component
class ExampleMapper {

    /**
     * Преобразование Domain Model -> Response DTO.
     */
    fun toResponse(entity: ExampleEntity): ExampleResponse {
        return ExampleResponse(
            id = entity.id.toString(),
            title = entity.title,
            description = entity.description,
            status = entity.status.name,
            statusDisplayName = entity.status.displayName,
            priority = entity.priority.name,
            priorityDisplayName = entity.priority.displayName,
            estimatedEffort = entity.estimatedEffort,
            actualEffort = entity.actualEffort,
            progressPercentage = entity.progressPercentage,
            isOverEstimated = entity.isOverEstimated,
            isCompleted = entity.isCompleted,
            canBeEdited = entity.canBeEdited(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            completedAt = entity.completedAt
        )
    }

    /**
     * Преобразование списка.
     */
    fun toResponseList(entities: List<ExampleEntity>): List<ExampleResponse> {
        return entities.map { toResponse(it) }
    }
}
