package com.yourapp.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Kill-e API Documentation")
                    .description("This is the API documentation for the Kill-e application.")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Kill-e Team")
                            .email("gobziii@yandex.ru")
                    )
            )
    }
}
