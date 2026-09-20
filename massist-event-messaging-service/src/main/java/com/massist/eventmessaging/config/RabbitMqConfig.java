package com.massist.eventmessaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    private final MessagingProperties properties;

    public RabbitMqConfig(MessagingProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public Queue eventsQueue() {
        return QueueBuilder.durable(properties.getQueue()).build();
    }

    @Bean
    public Binding eventsBinding(Queue eventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(eventsQueue)
                .to(eventsExchange)
                .with(properties.getRoutingKeyPattern());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.massist.eventmessaging");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
