package com.massist.eventmessaging.service;

import com.massist.eventmessaging.config.MessagingProperties;
import com.massist.eventmessaging.dto.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final Deque<EventMessage> recentEvents = new ConcurrentLinkedDeque<>();
    private final int bufferSize;

    public EventConsumer(MessagingProperties properties) {
        this.bufferSize = properties.getConsumedBufferSize();
    }

    @RabbitListener(queues = "${massist.messaging.queue}")
    public void consume(EventMessage event) {
        log.info("Consumed event id={} type={} routingKey={}", event.id(), event.eventType(), event.routingKey());
        recentEvents.addFirst(event);
        while (recentEvents.size() > bufferSize) {
            recentEvents.removeLast();
        }
    }

    public List<EventMessage> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }
}
