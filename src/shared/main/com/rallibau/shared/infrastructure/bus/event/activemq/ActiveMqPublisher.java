package com.rallibau.shared.infrastructure.bus.event.activemq;

import com.rallibau.shared.domain.Service;
import com.rallibau.shared.domain.bus.event.DomainEvent;
import com.rallibau.shared.infrastructure.bus.event.DomainEventJsonSerializer;
import org.springframework.amqp.AmqpException;
import org.springframework.jms.core.JmsTemplate;

@Service
public class ActiveMqPublisher {
    private final JmsTemplate jmsTemplate;

    public ActiveMqPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void publish(DomainEvent domainEvent, String exchangeName) throws AmqpException {
        String serializedDomainEvent = DomainEventJsonSerializer.serialize(domainEvent);


        jmsTemplate.convertAndSend(exchangeName, serializedDomainEvent);
    }


}
