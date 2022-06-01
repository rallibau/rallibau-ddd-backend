package com.rallibau.boundedContext.articulo.application.find;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rallibau.shared.domain.bus.query.Query;
import com.rallibau.shared.domain.bus.query.Response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

public final class ArticuloFinderQuery extends Query {
    private final String articuloId;

    public ArticuloFinderQuery() {
        this.articuloId = null;
    }

    public ArticuloFinderQuery(String articuloId) {

        this.articuloId = articuloId;
    }

    public String articuloId() {

        return articuloId;
    }


    @Override
    public HashMap<String, Serializable> toPrimitives() {
        return new HashMap<String, Serializable>() {{
            put("articuloId", articuloId);
        }};
    }

    @Override
    public Query fromPrimitives(String aggregateId, HashMap<String, Serializable> body, String eventId, String occurredOn) {
        return new ArticuloFinderQuery((String) body.get("articuloId"));
    }

    @Override
    public Response parseResponse(String message) throws JsonProcessingException {
        return Optional.of(new ObjectMapper().readValue(message, ArticuloResponse.class)).get();
    }
}
