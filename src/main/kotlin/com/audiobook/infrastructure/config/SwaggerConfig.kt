package com.audiobook.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Конфигурация Swagger/OpenAPI.
 *
 * SpringDoc автоматически сканирует контроллеры с аннотациями @RestController
 * и генерирует OpenAPI документацию.
 */
@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Kill-e API - Example Entity Demo")
                    .description("""
                        Демонстрация Clean Architecture + MVVM + DDD
                        
                        **Архитектурные слои:**
                        - Presentation Layer (Controllers, ViewModels)
                        - Application Layer (Use Cases, DTOs)
                        - Domain Layer (Rich Entities, Business Logic)
                        - Infrastructure Layer (JPA, Database)
                        
                        **ExampleEntity** - богатая доменная модель с бизнес-логикой
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Kill-e Team")
                            .email("dev@example.com")
                    )
            )
    }
}
