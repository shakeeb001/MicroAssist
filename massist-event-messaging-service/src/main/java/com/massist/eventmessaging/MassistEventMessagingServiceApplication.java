package com.massist.eventmessaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MassistEventMessagingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MassistEventMessagingServiceApplication.class, args);
    }
}
