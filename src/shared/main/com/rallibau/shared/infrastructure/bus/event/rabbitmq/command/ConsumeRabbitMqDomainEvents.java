package com.rallibau.shared.infrastructure.bus.event.rabbitmq.command;


import com.rallibau.shared.domain.Service;
import com.rallibau.shared.infrastructure.cli.ConsoleCommand;
import com.rallibau.shared.infrastructure.bus.event.rabbitmq.RabbitMqDomainEventsConsumer;


@Service
public final class ConsumeRabbitMqDomainEvents extends ConsoleCommand {
    private final RabbitMqDomainEventsConsumer eventConsumer;


    public ConsumeRabbitMqDomainEvents(RabbitMqDomainEventsConsumer eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    @Override
    public void execute(String[] args) {
        eventConsumer.consume();
    }
}
