package com.rallibau.apps.presentaciones.controller.articulo;

import com.rallibau.boundedContext.articulo.application.find.ArticuloFinderQuery;
import com.rallibau.boundedContext.articulo.domain.ArticuloNotFound;
import com.rallibau.shared.domain.bus.command.CommandBus;
import com.rallibau.shared.domain.bus.query.QueryBus;
import com.rallibau.shared.infrastructure.spring.api.ApiController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class ArticuloGetController extends ApiController {


    public ArticuloGetController(QueryBus queryBus,
                                 CommandBus commandBus) {

        super(queryBus, commandBus);
    }

    @GetMapping("/articulo/{articuloId}")
    public ResponseEntity<?> byId(@PathVariable String articuloId) {
        return ResponseEntity.ok(ask(new ArticuloFinderQuery(articuloId)));
    }


    @Override
    public HashMap<Class<? extends RuntimeException>, HttpStatus> errorMapping() {
        return new HashMap<Class<? extends RuntimeException>, HttpStatus>() {{
            put(ArticuloNotFound.class, HttpStatus.NOT_FOUND);
        }};
    }
}
