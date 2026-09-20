package com.massist.eventmessaging.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventMessagingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Massist Event Messaging Service")
                        .description("REST API for producing RabbitMQ events and inspecting recently consumed messages.")
                        .version("v1"));
    }
}
