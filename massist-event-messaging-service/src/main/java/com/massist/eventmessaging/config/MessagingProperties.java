package com.massist.eventmessaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "massist.messaging")
public class MessagingProperties {

    private String exchange = "massist.events";
    private String queue = "massist.events.queue";
    private String routingKeyPattern = "massist.event.#";
    private String defaultRoutingKey = "massist.event.created";
    private int consumedBufferSize = 50;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getRoutingKeyPattern() {
        return routingKeyPattern;
    }

    public void setRoutingKeyPattern(String routingKeyPattern) {
        this.routingKeyPattern = routingKeyPattern;
    }

    public String getDefaultRoutingKey() {
        return defaultRoutingKey;
    }

    public void setDefaultRoutingKey(String defaultRoutingKey) {
        this.defaultRoutingKey = defaultRoutingKey;
    }

    public int getConsumedBufferSize() {
        return consumedBufferSize;
    }

    public void setConsumedBufferSize(int consumedBufferSize) {
        this.consumedBufferSize = consumedBufferSize;
    }
}
