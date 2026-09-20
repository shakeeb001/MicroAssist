package com.massist.eventmessaging.controller;

import com.massist.eventmessaging.dto.EventMessage;
import com.massist.eventmessaging.dto.PublishEventRequest;
import com.massist.eventmessaging.service.EventConsumer;
import com.massist.eventmessaging.service.EventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Publish RabbitMQ events and inspect recently consumed messages")
public class EventController {

    private final EventPublisher eventPublisher;
    private final EventConsumer eventConsumer;

    public EventController(EventPublisher eventPublisher, EventConsumer eventConsumer) {
        this.eventPublisher = eventPublisher;
        this.eventConsumer = eventConsumer;
    }

    @PostMapping
    @Operation(summary = "Publish an event to RabbitMQ")
    @ApiResponse(responseCode = "201", description = "Event published")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<EventMessage> publish(@Valid @RequestBody PublishEventRequest request) {
        EventMessage published = eventPublisher.publish(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(published);
    }

    @GetMapping("/consumed")
    @Operation(summary = "List recently consumed events")
    @ApiResponse(responseCode = "200", description = "Recent in-memory consumed events, newest first")
    public List<EventMessage> consumed() {
        return eventConsumer.getRecentEvents();
    }
}
