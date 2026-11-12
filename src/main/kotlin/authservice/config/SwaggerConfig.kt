package com.aifactory.authservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()

            .info(
                Info()
                    .title("API 문서")
                    .description("인증/인가 API 문서입니다.")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("SE팀")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("https://gateway-prod.purpleriver-9377ba31.koreacentral.azurecontainerapps.io/")
                        .description("PROD Gateway Server"),
                    Server()
                        .url("/")
                        .description("Current Server"),
                    Server()
                        .url("http://localhost:8080")
                        .description("Local Gateway Server")
                )
            )
    }
}