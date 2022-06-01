package com.rallibau.shared.infrastructure.bus.event.rabbitmq;

import com.rallibau.shared.infrastructure.bus.event.DomainEventSubscribersInformation;
import com.rallibau.shared.infrastructure.bus.event.DomainEventsInformation;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class RabbitMqConfiguration {
    @Value("${mq.events.provider}")
    private String MQ_EVENTS_PROVIDER;
    @Value("${mq.event.exchange}")
    private String RABBITMQ_EVENT_EXCHANGE;
    @Value("${rabbit.host}")
    private String RABBITMQ_HOST;
    @Value("${rabbit.port}")
    private String RABBITMQ_PORT;
    @Value("${rabbit.login}")
    private String RABBITMQ_LOGIN;
    @Value("${rabbit.password}")
    private String RABBITMQ_PASSWORD;

    public static final String RABBIT = "rabbit";

    private final DomainEventSubscribersInformation domainEventSubscribersInformation;
    private final DomainEventsInformation domainEventsInformation;


    public RabbitMqConfiguration(
            DomainEventSubscribersInformation domainEventSubscribersInformation,
            DomainEventsInformation domainEventsInformation) {
        this.domainEventSubscribersInformation = domainEventSubscribersInformation;
        this.domainEventsInformation = domainEventsInformation;
    }

    @Bean
    public CachingConnectionFactory connection() {
        CachingConnectionFactory factory = new CachingConnectionFactory();

        factory.setHost(RABBITMQ_HOST);
        factory.setPort(Integer.parseInt(RABBITMQ_PORT));
        factory.setUsername(RABBITMQ_LOGIN);
        factory.setPassword(RABBITMQ_PASSWORD);

        return factory;
    }

    @Bean
    public Declarables declaration() {
        List<Declarable> declarable = new ArrayList<>();

        if (RABBIT.equals(MQ_EVENTS_PROVIDER)) {
            declarable = obtainsDeclarableOfExchange(RABBITMQ_EVENT_EXCHANGE);
        }


        return new Declarables(declarable);
    }


    private List<Declarable> obtainsDeclarableOfExchange(String exchangeName) {
        String retryExchangeName = RabbitMqExchangeNameFormatter.retry(exchangeName);
        String deadLetterExchangeName = RabbitMqExchangeNameFormatter.deadLetter(exchangeName);

        TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        TopicExchange retryExchange = new TopicExchange(retryExchangeName, true, false);
        TopicExchange deadLetterExchange = new TopicExchange(deadLetterExchangeName, true, false);
        List<Declarable> declarable = new ArrayList<>();
        declarable.add(exchange);
        declarable.add(retryExchange);
        declarable.add(deadLetterExchange);

        if (exchangeName.equals(RABBITMQ_EVENT_EXCHANGE)) {
            Collection<Declarable> queuesAndBindings = declareQueuesAndBindingsEvents(
                    exchange,
                    retryExchange,
                    deadLetterExchange
            ).stream().flatMap(Collection::stream).collect(Collectors.toList());
            declarable.addAll(queuesAndBindings);
        }


        return declarable;
    }


    private Collection<Collection<Declarable>> declareQueuesAndBindingsEvents(
            TopicExchange topicExchange,
            TopicExchange retryTopicExchange,
            TopicExchange deadLetterTopicExchange
    ) {

        return domainEventSubscribersInformation.all().stream().map(information -> {
            String queueName = RabbitMqEventQueueNameFormatter.format(information);
            String retryQueueName = RabbitMqEventQueueNameFormatter.formatRetry(information);
            String deadLetterQueueName = RabbitMqEventQueueNameFormatter.formatDeadLetter(information);

            Queue queue = QueueBuilder.durable(queueName).build();
            Queue retryQueue = QueueBuilder.durable(retryQueueName).withArguments(
                    retryQueueArguments(topicExchange, queueName)
            ).build();
            Queue deadLetterQueue = QueueBuilder.durable(deadLetterQueueName).build();

            Binding fromExchangeSameQueueBinding = BindingBuilder
                    .bind(queue)
                    .to(topicExchange)
                    .with(queueName);

            Binding fromRetryExchangeSameQueueBinding = BindingBuilder
                    .bind(retryQueue)
                    .to(retryTopicExchange)
                    .with(queueName);

            Binding fromDeadLetterExchangeSameQueueBinding = BindingBuilder
                    .bind(deadLetterQueue)
                    .to(deadLetterTopicExchange)
                    .with(queueName);

            List<Binding> fromExchangeDomainEventsBindings = information.subscribedEvents().stream().map(
                    domainEventClass -> {
                        String eventName = domainEventsInformation.forClass(domainEventClass);
                        return BindingBuilder
                                .bind(queue)
                                .to(topicExchange)
                                .with(eventName);
                    }).collect(Collectors.toList());

            List<Declarable> queuesAndBindings = new ArrayList<>();
            queuesAndBindings.add(queue);
            queuesAndBindings.add(fromExchangeSameQueueBinding);
            queuesAndBindings.addAll(fromExchangeDomainEventsBindings);

            queuesAndBindings.add(retryQueue);
            queuesAndBindings.add(fromRetryExchangeSameQueueBinding);

            queuesAndBindings.add(deadLetterQueue);
            queuesAndBindings.add(fromDeadLetterExchangeSameQueueBinding);

            return queuesAndBindings;
        }).collect(Collectors.toList());
    }

    private HashMap<String, Object> retryQueueArguments(TopicExchange exchange, String routingKey) {
        return new HashMap<String, Object>() {{
            put("x-dead-letter-exchange", exchange.getName());
            put("x-dead-letter-routing-key", routingKey);
            put("x-message-ttl", 1000);
        }};
    }
}
