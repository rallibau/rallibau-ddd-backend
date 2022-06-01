package com.rallibau.boundedContext.articulo.domain;

import com.rallibau.shared.domain.WordMother;

public class ArticuloNameMother {
    public static ArticuloName random() {

        return ArticuloName.create(WordMother.random());
    }

    private static ArticuloName create(String name) {
        return ArticuloName.create(name);
    }
}
