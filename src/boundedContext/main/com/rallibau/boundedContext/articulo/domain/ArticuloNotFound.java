package com.rallibau.boundedContext.articulo.domain;

import com.rallibau.shared.domain.DomainError;

public class ArticuloNotFound extends DomainError {
    public ArticuloNotFound(ArticuloId id) {
        super("DRUG_NOT_FOUND", "The drug with id ".concat(id.value()).concat(" not exist"));
    }
}
