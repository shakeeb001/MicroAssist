package com.massist.eventmessaging.dto;

import java.time.Instant;
import java.util.Map;

public record EventMessage(
        String id,
        Instant timestamp,
        String source,
        String eventType,
        String routingKey,
        Map<String, Object> payload
) {
}
