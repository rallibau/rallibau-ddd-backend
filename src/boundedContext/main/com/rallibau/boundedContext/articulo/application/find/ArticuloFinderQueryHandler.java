package com.rallibau.boundedContext.articulo.application.find;

import com.rallibau.boundedContext.articulo.domain.Articulo;
import com.rallibau.shared.domain.Service;
import com.rallibau.shared.domain.bus.query.QueryHandler;
import com.rallibau.shared.domain.bus.query.QueryHandlerExecutionError;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("prescripciones-transaction_manager")
public class ArticuloFinderQueryHandler implements QueryHandler<ArticuloFinderQuery, ArticuloResponse> {


    private final ArticuloFinder articuloFinder;

    public ArticuloFinderQueryHandler(ArticuloFinder articuloFinder) {

        this.articuloFinder = articuloFinder;
    }

    @Override
    public ArticuloResponse handle(ArticuloFinderQuery query) throws QueryHandlerExecutionError {
        Articulo articulo = articuloFinder.find(query.articuloId());


        return new ArticuloResponse(articulo.id().value(),
                articulo.name().value());

    }
}
