package com.example.ms_category.configs

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Category Microservice API")
                    .version("1.0.0")
                    .description("API para gerenciamento de categorias - Spring Boot 4")
                    .contact(
                        Contact()
                            .name("Time de Engenharia")
                            .email("eng@example.com")
                    )
                    .license(
                        License().name("MIT").url("https://opensource.org/licenses/MIT")
                    )
            )
//            .components(
//                Components().addSecuritySchemes(
//                    "BearerAuth",
//                    SecurityScheme()
//                        .type(SecurityScheme.Type.HTTP)
//                        .scheme("bearer")
//                        .bearerFormat("JWT")
//                )
//            )
    }
}