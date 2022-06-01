package com.rallibau.boundedContext.articulo.domain;

import com.rallibau.shared.domain.StringValueObject;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public final class ArticuloName extends StringValueObject {
    public ArticuloName(String value) {

        super(value);
    }

    public ArticuloName() {

        super("");
    }

    public static ArticuloName create(String name) {

        return new ArticuloName(name);
    }
}
