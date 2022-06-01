package com.rallibau.shared.infrastructure.bus.event.rabbitmq;

import com.rallibau.shared.domain.Service;
import com.rallibau.shared.domain.Utils;
import com.rallibau.shared.infrastructure.bus.event.DomainEventJsonDeserializer;
import com.rallibau.shared.infrastructure.bus.event.DomainEventSubscribersInformation;
import com.rallibau.shared.domain.bus.event.DomainEvent;
import com.rallibau.shared.domain.bus.event.SubscriberNotRegisteredError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitMqDomainEventsConsumer {
    public static final String DEAD_LETTER_SUBSCRIBER = "deadLetterListener";
    public static final String DEAD_LETTER = "dead_letter";
    private final String CONSUMER_NAME = "domain_events_consumer";
    private final String CONSUMER_NAME_DEAD_LETTER = "dead_letter-domain_events_consumer";
    private final DomainEventJsonDeserializer deserializer;
    private final ApplicationContext context;
    private final RabbitMqPublisher publisher;
    private final HashMap<String, Object> domainEventSubscribers = new HashMap<>();
    RabbitListenerEndpointRegistry registry;
    private DomainEventSubscribersInformation information;
    private static final Logger logger = LogManager.getLogger(RabbitMqDomainEventsConsumer.class);

    public RabbitMqDomainEventsConsumer(
            RabbitListenerEndpointRegistry registry,
            DomainEventSubscribersInformation information,
            DomainEventJsonDeserializer deserializer,
            ApplicationContext context,
            RabbitMqPublisher publisher
    ) {
        this.registry = registry;
        this.information = information;
        this.deserializer = deserializer;
        this.context = context;
        this.publisher = publisher;
    }

    public void consume() {
        AbstractMessageListenerContainer container = (AbstractMessageListenerContainer) registry.getListenerContainer(
                CONSUMER_NAME
        );

        container.addQueueNames(information.rabbitMqFormattedNames());

        container.start();

        AbstractMessageListenerContainer containerDeadLetter = (AbstractMessageListenerContainer) registry.getListenerContainer(
                CONSUMER_NAME_DEAD_LETTER
        );

        containerDeadLetter.addQueueNames(information.rabbitMqFormattedNamesDeadLetter());

        containerDeadLetter.start();
    }

    @RabbitListener(id = CONSUMER_NAME, autoStartup = "true")
    public void consumer(Message message) throws Exception {
        String serializedMessage = new String(message.getBody());
        DomainEvent domainEvent = null;
        String queue = null;
        try {
            queue = message.getMessageProperties().getConsumerQueue();
            if (queue == null || queue.isEmpty()) {
                return;
            }
            domainEvent = deserializer.deserialize(serializedMessage);

            Object subscriber = domainEventSubscribers.containsKey(queue)
                    ? domainEventSubscribers.get(queue)
                    : subscriberFor(queue);
            getOnMethod(domainEvent, subscriber, queue).invoke(subscriber, domainEvent);
        } catch (SubscriberNotRegisteredError error) {
            //empty
        } catch (Exception error) {
            if (queue != null) {
                handleConsumptionError(message, queue, getMessageError(error));
            }

        }
    }

    @RabbitListener(id = CONSUMER_NAME_DEAD_LETTER, autoStartup = "true")
    public void consumerDeadLetter(Message message) throws Exception {
        DomainEvent domainEvent = null;
        String serializedMessage = new String(message.getBody());
        domainEvent = deserializer.deserialize(serializedMessage);
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        ArrayList<String> errorList = (ArrayList<String>) headers.getOrDefault("errors_list", new ArrayList<String>());

        Object subscriber = subscriberForDeadLetter();
        subscriber.getClass().getMethod("on", DomainEvent.class, ArrayList.class).invoke(subscriber, domainEvent, errorList);

    }

    private String getMessageError(Exception e) {
        if (e instanceof Throwable) {
            return e.getCause().getMessage();
        } else {
            return e.getMessage();
        }
    }

    private Method getOnMethod(DomainEvent domainEvent, Object subscriber, String queue) throws Exception {
        try {
            return subscriber.getClass().getMethod("on", domainEvent.getClass());
        } catch (NoSuchMethodException e) {
            throw new Exception(String.format(
                    "The subscriber <%s> should implement a method `on` listening the domain event <%s>",
                    queue,
                    domainEvent.eventName()
            ));
        }
    }

    public void withSubscribersInformation(DomainEventSubscribersInformation information) {
        this.information = information;
    }

    private void handleConsumptionError(DomainEvent event, String queue) {

    }

    private void handleConsumptionError(Message message, String queue, String errorDesc) {
        if (hasBeenRedeliveredTooMuch(message)) {
            sendToDeadLetter(message, queue, errorDesc);
        } else {
            sendToRetry(message, queue, errorDesc);
        }
    }


    private void sendToRetry(Message message, String queue, String errorDesc) {
        sendMessageTo(RabbitMqExchangeNameFormatter.retry("domain_events"), message, queue, errorDesc);
    }

    private void sendToDeadLetter(Message message, String queue, String errorDesc) {
        sendMessageTo(RabbitMqExchangeNameFormatter.deadLetter("domain_events"), message, queue, errorDesc);
    }

    private void sendMessageTo(String exchange, Message message, String queue, String errorDesc) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();

        headers.put("redelivery_count", (int) headers.getOrDefault("redelivery_count", 0) + 1);

        anotateError(errorDesc, headers);

        MessageBuilder.fromMessage(message).andProperties(
                MessagePropertiesBuilder.newInstance()
                        .setContentEncoding("utf-8")
                        .setContentType("application/json")
                        .copyHeaders(headers)
                        .build());

        publisher.rePublish(message, exchange, queue);
    }

    private void anotateError(String errorDesc, Map<String, Object> headers) {
        if (errorDesc == null || errorDesc.isEmpty()) {
            return;
        }
        ArrayList<String> errorList = (ArrayList<String>) headers.getOrDefault("errors_list", new ArrayList<String>());
        errorList.add(errorDesc);
        headers.put("errors_list", errorList);

    }

    private boolean hasBeenRedeliveredTooMuch(Message message) {
        int MAX_RETRIES = 2;
        return (int) message.getMessageProperties().getHeaders().getOrDefault("redelivery_count", 0) >= MAX_RETRIES;
    }

    private Object subscriberFor(String queue) throws SubscriberNotRegisteredError {
        String[] queueParts = queue.split("\\.");
        String subscriberName = Utils.toCamelFirstLower(queueParts[queueParts.length - 1]);
        try {
            Object subscriber = context.getBean(subscriberName);
            domainEventSubscribers.put(queue, subscriber);
            return subscriber;
        } catch (Exception e) {
            throw new SubscriberNotRegisteredError(queue);
        }
    }

    private Object subscriberForDeadLetter() throws SubscriberNotRegisteredError {
        try {
            return context.getBean(DEAD_LETTER_SUBSCRIBER);
        } catch (Exception e) {
            logger.error("No hay un subscriber para las dead_letter", e);
            throw new SubscriberNotRegisteredError(DEAD_LETTER);
        }
    }
}
