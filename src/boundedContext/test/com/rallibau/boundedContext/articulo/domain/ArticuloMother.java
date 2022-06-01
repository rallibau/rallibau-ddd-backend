package com.rallibau.boundedContext.articulo.domain;

public final class ArticuloMother {
    public static Articulo create(ArticuloId id,
                                  ArticuloName name) {
        return Articulo.create(id, name);
    }

    public static Articulo random() {
        return Articulo.create(
                ArticuloIdMother.random(),
                ArticuloNameMother.random());
    }

    public static Articulo random(String idArticulo) {
        return Articulo.create(
                ArticuloId.create(idArticulo),
                ArticuloNameMother.random());
    }
}
