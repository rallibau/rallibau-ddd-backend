package com.rallibau.boundedContext.articulo.domain;

import com.rallibau.shared.domain.Identifier;

import javax.persistence.Embeddable;

@Embeddable
public final class ArticuloId extends Identifier {
    public ArticuloId(String value) {
        super(value);
    }

    public ArticuloId() {
    }

    public static ArticuloId create(String value) {
        return new ArticuloId(value);
    }

    @Override
    public void ensureValidUuid(String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

}
