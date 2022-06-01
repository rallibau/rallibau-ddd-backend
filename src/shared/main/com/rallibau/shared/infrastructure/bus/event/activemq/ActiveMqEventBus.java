package com.rallibau.shared.infrastructure.bus.event.activemq;

import com.rallibau.shared.domain.bus.event.DomainEvent;
import com.rallibau.shared.domain.bus.event.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.AmqpException;

import java.util.List;

public class ActiveMqEventBus implements EventBus {

    private static final Logger logger = LogManager.getLogger(ActiveMqEventBus.class);
    private final ActiveMqPublisher publisher;
    private final String exchangeName;

    public ActiveMqEventBus(ActiveMqPublisher publisher) {
        this.publisher = publisher;
        this.exchangeName = "domain_events";
    }

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(this::publish);
    }

    @Override
    public void publish(DomainEvent domainEvent) {
        try {
            this.publisher.publish(domainEvent, exchangeName);
        } catch (AmqpException error) {
            //TODO: cola de errores
            logger.error(error.getMessage(), error);
        }
    }
}
