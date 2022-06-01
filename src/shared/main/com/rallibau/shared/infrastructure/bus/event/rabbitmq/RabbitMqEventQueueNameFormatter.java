package com.rallibau.shared.infrastructure.bus.event.rabbitmq;

import com.rallibau.shared.infrastructure.bus.event.DomainEventSubscriberInformation;

public final class RabbitMqEventQueueNameFormatter {
    public static String format(DomainEventSubscriberInformation information) {
        return information.formatRabbitMqQueueName();
    }

    public static String formatRetry(DomainEventSubscriberInformation information) {
        return String.format("retry.%s", format(information));
    }

    public static String formatDeadLetter(DomainEventSubscriberInformation information) {
        return String.format("dead_letter.%s", format(information));
    }


}
