package com.rallibau.shared.infrastructure.bus.event.activemq.command;


import com.rallibau.shared.domain.Service;
import com.rallibau.shared.infrastructure.cli.ConsoleCommand;
import com.rallibau.shared.infrastructure.bus.event.activemq.ActiveMqDomainEventsConsumer;


@Service
public final class ConsumeActiveMqDomainEvents extends ConsoleCommand {
    private final ActiveMqDomainEventsConsumer receiver;


    public ConsumeActiveMqDomainEvents(ActiveMqDomainEventsConsumer receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute(String[] args) {
        receiver.consume();
    }
}
