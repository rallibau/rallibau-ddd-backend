package com.rallibau.boundedContext.articulo.domain;

import com.rallibau.shared.domain.UuidMother;

public class ArticuloIdMother {
    public static ArticuloId random() {
        return ArticuloIdMother.create(UuidMother.random());
    }

    private static ArticuloId create(String id) {
        return ArticuloId.create(id);
    }
}
