package com.rallibau.shared.infrastructure.bus.command.inMemory;

import com.rallibau.shared.domain.Service;
import com.rallibau.shared.infrastructure.bus.command.CommandHandlersInformation;
import com.rallibau.shared.domain.bus.command.CommandBus;
import com.rallibau.shared.domain.bus.command.CommandHandler;
import com.rallibau.shared.domain.bus.command.CommandHandlerExecutionError;
import com.rallibau.shared.domain.bus.command.Command;
import org.springframework.context.ApplicationContext;

@Service
public class InMemoryCommandBus implements CommandBus {

    private final CommandHandlersInformation information;
    private final ApplicationContext context;

    public InMemoryCommandBus(CommandHandlersInformation information, ApplicationContext context) {
        this.information = information;
        this.context = context;
    }

    @Override
    public void dispatch(Command command) throws CommandHandlerExecutionError {
        try {
            Class<? extends CommandHandler> commandHandlerClass = information.search(command.getClass());

            CommandHandler handler = context.getBean(commandHandlerClass);

            handler.handle(command);
        } catch (Throwable error) {
            throw new CommandHandlerExecutionError(error);
        }
    }

    @Override
    public void asynchronousDispatch(Command command) throws CommandHandlerExecutionError {
        //TODO
    }
}
