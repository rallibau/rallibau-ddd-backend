package com.rallibau.shared.infrastructure.bus.event.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class ActiveMqConfiguration {
    @Value("${mq.events.provider}")
    private String MQ_EVENTS_PROVIDER;


    @Value("${activemq.host}")
    private String ACTIVEMQ_HOST;
    @Value("${activemq.port}")
    private String ACTIVEMQ_PORT;
    @Value("${activemq.login}")
    private String ACTIVEMQ_LOGIN;
    @Value("${activemq.password}")
    private String ACTIVEMQ_PASSWORD;

    public static final String ACTIVEMQ = "activemq";


    private final ActiveMqDomainEventsConsumer activeMqDomainEventsConsumer;


    public ActiveMqConfiguration(ActiveMqDomainEventsConsumer activeMqDomainEventsConsumer) {
        this.activeMqDomainEventsConsumer = activeMqDomainEventsConsumer;
    }

    @Bean
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL("tcp://".concat(ACTIVEMQ_HOST).concat(":").concat(ACTIVEMQ_PORT));

        return activeMQConnectionFactory;
    }


    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {


        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();
        factory
                .setConnectionFactory(senderActiveMQConnectionFactory());
        factory.setAutoStartup(false);

        factory.setSessionTransacted(true);
        factory.setConcurrency("5");

        return factory;
    }




    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        return new CachingConnectionFactory(
                senderActiveMQConnectionFactory());
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        return new JmsTemplate(cachingConnectionFactory());
    }

}
