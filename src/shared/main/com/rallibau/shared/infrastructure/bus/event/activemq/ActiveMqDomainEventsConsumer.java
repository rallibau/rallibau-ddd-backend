package com.rallibau.shared.infrastructure.bus.event.activemq;

import com.rallibau.shared.domain.DomainError;
import com.rallibau.shared.domain.Service;
import com.rallibau.shared.domain.Utils;
import com.rallibau.shared.infrastructure.bus.event.DomainEventJsonDeserializer;
import com.rallibau.shared.infrastructure.bus.event.DomainEventSubscribersInformation;
import com.rallibau.shared.domain.bus.event.DomainEvent;
import com.rallibau.shared.domain.bus.event.DomainEventSubscriber;
import com.rallibau.shared.domain.bus.event.SubscriberNotRegisteredError;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.MessageListenerContainer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


@Service
public class ActiveMqDomainEventsConsumer {

    private final String CONSUMER_NAME = "domain_events_consumer";
    @Value("${base.package}")
    private String BASE_PACKAGE;


    private final DomainEventJsonDeserializer deserializer;
    private final DomainEventSubscribersInformation information;
    private final HashMap<String, List<Object>> domainEventSubscribers = new HashMap<>();
    private final ApplicationContext context;
    private final JmsListenerEndpointRegistry registry;



    public ActiveMqDomainEventsConsumer(DomainEventJsonDeserializer deserializer,
                                        DomainEventSubscribersInformation information,
                                        ApplicationContext context,
                                        JmsListenerEndpointRegistry registry) {
        this.deserializer = deserializer;
        this.information = information;
        this.context = context;
        this.registry = registry;

    }


    public void consume() {
        MessageListenerContainer container = registry.getListenerContainer(
                CONSUMER_NAME
        );


        container.start();

    }


    @JmsListener(destination = "domain_events", id = CONSUMER_NAME)
    public void consumer(String message) throws SubscriberNotRegisteredError,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        DomainEvent domainEvent = null;
        String queue = null;
        try {

            domainEvent = deserializer.deserialize(message);
            queue = domainEvent.getClass().getCanonicalName();
            if (queue == null || queue.isEmpty()) {
                return;
            }

            List<Object> subscribers = domainEventSubscribers.containsKey(queue)
                    ? domainEventSubscribers.get(queue)
                    : subscriberFor(queue);

            for (Object subscriber : subscribers) {
                getOnMethod(domainEvent, subscriber, queue).invoke(subscriber, domainEvent);
            }
        } catch (DomainError error) {
            if (queue != null) {
                handleConsumptionError(domainEvent, getMessageError(error));
            }

        }
    }

    private String getMessageError(Exception e) {
        if (e instanceof Throwable) {
            return e.getCause().getMessage();
        } else {
            return e.getMessage();
        }
    }

    private Method getOnMethod(DomainEvent domainEvent, Object subscriber, String queue) throws NoSuchMethodException {
        return subscriber.getClass().getMethod("on", domainEvent.getClass());
    }

    private List<Object> subscriberFor(String queue) throws SubscriberNotRegisteredError {
        Reflections reflections = new Reflections(BASE_PACKAGE);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(DomainEventSubscriber.class);
        if (classes.isEmpty()) {
            throw new SubscriberNotRegisteredError(queue);
        }

        ArrayList<Class> claseSubscriberList = new ArrayList<>();
        for (Class clase : classes) {
            DomainEventSubscriber subscriber = (DomainEventSubscriber) clase.getAnnotation(DomainEventSubscriber.class);
            Class<? extends DomainEvent>[] events = subscriber.value();
            for (Class event : events) {
                if (event.getCanonicalName().equals(queue)) {
                    claseSubscriberList.add(clase);
                    break;
                }
            }

        }

        if (claseSubscriberList.isEmpty()) {
            throw new SubscriberNotRegisteredError(queue);
        }

        try {
            ArrayList<Object> subscribers = new ArrayList<>();
            for (Class claseSubscriber : claseSubscriberList) {
                String[] subscriberParts = claseSubscriber.getName().split("\\.");
                String subscriberName = Utils.firstToLower(subscriberParts[subscriberParts.length - 1]);
                Object subscriber = context.getBean(subscriberName);
                subscribers.add(subscriber);

            }
            domainEventSubscribers.put(queue, subscribers);


            return subscribers;
        } catch (Exception e) {
            throw new SubscriberNotRegisteredError(queue);
        }
    }

    private void handleConsumptionError(DomainEvent domainEvent, String error) {

    }
}
