package com.massist.eventmessaging.service;

import com.massist.eventmessaging.config.MessagingProperties;
import com.massist.eventmessaging.dto.EventMessage;
import com.massist.eventmessaging.dto.PublishEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private static final String DEFAULT_SOURCE = "massist-event-messaging-service";

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;

    public EventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public EventMessage publish(PublishEventRequest request) {
        String routingKey = hasText(request.routingKey())
                ? request.routingKey()
                : properties.getDefaultRoutingKey();
        String source = hasText(request.source()) ? request.source() : DEFAULT_SOURCE;
        Map<String, Object> payload = request.payload() != null ? request.payload() : Map.of();

        EventMessage message = new EventMessage(
                UUID.randomUUID().toString(),
                Instant.now(),
                source,
                request.eventType(),
                routingKey,
                payload
        );

        rabbitTemplate.convertAndSend(properties.getExchange(), routingKey, message);
        log.info("Published event id={} type={} routingKey={}", message.id(), message.eventType(), routingKey);
        return message;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
