package com.massist.eventmessaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@Schema(description = "Request body used to publish an event onto RabbitMQ")
public record PublishEventRequest(
        @NotBlank
        @Schema(description = "Logical event type", example = "user.registered")
        String eventType,

        @Schema(description = "AMQP routing key. Defaults to massist.event.created", example = "massist.event.created")
        String routingKey,

        @Schema(description = "Originating service or producer name", example = "massist-user-service")
        String source,

        @Schema(description = "Arbitrary JSON payload attached to the event")
        Map<String, Object> payload
) {
}
