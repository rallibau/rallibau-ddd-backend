package com.rallibau.apps.commons.config.web;


import com.rallibau.shared.domain.Service;
import com.rallibau.shared.infrastructure.bus.event.activemq.command.ConsumeActiveMqDomainEvents;
import com.rallibau.shared.infrastructure.bus.event.rabbitmq.command.ConsumeRabbitMqDomainEvents;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import static com.rallibau.shared.infrastructure.bus.event.activemq.ActiveMqConfiguration.ACTIVEMQ;

@Service
public class MqConsumerAutoStarter implements ApplicationListener<ApplicationReadyEvent> {
    private static final String RABBIT = "rabbit";
    private final ConsumeRabbitMqDomainEvents consumeRabbitMqDomainEventsCommand;
    private final ConsumeActiveMqDomainEvents consumeActiveMqDomainEvents;

    @Value("${mq.events.provider}")
    private String MQ_EVENTS_PROVIDER;


    public MqConsumerAutoStarter(ConsumeRabbitMqDomainEvents consumeRabbitMqDomainEventsCommand, ConsumeActiveMqDomainEvents consumeActiveMqDomainEvents) {
        this.consumeRabbitMqDomainEventsCommand = consumeRabbitMqDomainEventsCommand;
        this.consumeActiveMqDomainEvents = consumeActiveMqDomainEvents;
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (RABBIT.equals(MQ_EVENTS_PROVIDER)) {
            consumeRabbitMqDomainEventsCommand.execute(new String[0]);
        }

        if(ACTIVEMQ.equals(MQ_EVENTS_PROVIDER) ){
            consumeActiveMqDomainEvents.execute(new String[0]);
        }
    }
}