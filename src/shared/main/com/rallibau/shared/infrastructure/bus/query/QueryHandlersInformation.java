package com.rallibau.shared.infrastructure.bus.query;

import com.rallibau.shared.domain.Service;
import com.rallibau.shared.domain.bus.query.Query;
import com.rallibau.shared.domain.bus.query.QueryHandler;
import com.rallibau.shared.domain.bus.query.QueryNotRegisteredError;
import org.reflections.Reflections;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@Service
public class QueryHandlersInformation {
    HashMap<Class<? extends Query>, Class<? extends QueryHandler>> indexedQueryHandlers;

    public QueryHandlersInformation() {
        Reflections reflections = new Reflections("com.rallibau");
        Set<Class<? extends QueryHandler>> classes = reflections.getSubTypesOf(QueryHandler.class);

        indexedQueryHandlers = formatHandlers(classes);
    }

    public Class<? extends QueryHandler> search(Class<? extends Query> queryClass) throws QueryNotRegisteredError {
        Class<? extends QueryHandler> queryHandlerClass = indexedQueryHandlers.get(queryClass);

        if (null == queryHandlerClass) {
            throw new QueryNotRegisteredError(queryClass);
        }

        return queryHandlerClass;
    }

    private HashMap<Class<? extends Query>, Class<? extends QueryHandler>> formatHandlers(
            Set<Class<? extends QueryHandler>> queryHandlers
    ) {
        HashMap<Class<? extends Query>, Class<? extends QueryHandler>> handlers = new HashMap<>();

        for (Class<? extends QueryHandler> handler : queryHandlers) {
            ParameterizedType paramType = (ParameterizedType) handler.getGenericInterfaces()[0];
            Class<? extends Query> queryClass = (Class<? extends Query>) paramType.getActualTypeArguments()[0];

            handlers.put(queryClass, handler);
        }

        return handlers;
    }

    public HashMap<Class<? extends Query>, Class<? extends QueryHandler>> all() {
        return indexedQueryHandlers;
    }


    public Optional<Class<? extends Query>> forName(String name) {
        return indexedQueryHandlers.keySet().stream().filter(query -> query.getName().equals(name)).findFirst();
    }
}
