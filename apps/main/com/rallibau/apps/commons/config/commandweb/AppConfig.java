package com.rallibau.apps.commons.config.commandweb;

import com.rallibau.shared.domain.bus.event.EventBus;
import com.rallibau.shared.infrastructure.bus.event.activemq.ActiveMqEventBus;
import com.rallibau.shared.infrastructure.bus.event.activemq.ActiveMqPublisher;
import com.rallibau.shared.infrastructure.bus.event.rabbitmq.RabbitMqEventBus;
import com.rallibau.shared.infrastructure.bus.event.rabbitmq.RabbitMqPublisher;
import com.rallibau.shared.infrastructure.bus.event.spring.SpringApplicationEventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.rallibau.shared.infrastructure.bus.event.activemq.ActiveMqConfiguration.ACTIVEMQ;
import static com.rallibau.shared.infrastructure.bus.event.rabbitmq.RabbitMqConfiguration.RABBIT;

@Configuration
public class AppConfig {


    private final RabbitMqPublisher rabbitMqPublisher;
    private final ActiveMqPublisher activeMqPublisher;
    private final ApplicationEventPublisher publisher;

    @Value("${mq.events.provider}")
    private String MQ_EVENTS_PROVIDER;

    public AppConfig(RabbitMqPublisher rabbitMqPublisher,
                     ActiveMqPublisher activeMqPublisher,
                     ApplicationEventPublisher publisher) {
        this.rabbitMqPublisher = rabbitMqPublisher;
        this.activeMqPublisher = activeMqPublisher;
        this.publisher = publisher;
    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "OPTIONS");
            }
        };
    }

    @Bean
    public EventBus eventBus() {
        if (RABBIT.equals(MQ_EVENTS_PROVIDER)) {
            return new RabbitMqEventBus(rabbitMqPublisher);
        }

        if (ACTIVEMQ.equals(MQ_EVENTS_PROVIDER)) {
            return new ActiveMqEventBus(activeMqPublisher);
        }

        return new SpringApplicationEventBus(publisher);
    }

}
