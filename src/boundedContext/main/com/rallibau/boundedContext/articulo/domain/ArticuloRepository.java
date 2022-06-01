package com.rallibau.boundedContext.articulo.domain;

import com.rallibau.shared.domain.criteria.Criteria;

import java.util.List;
import java.util.Optional;

public interface ArticuloRepository {
    void save(Articulo articulo);

    Optional<Articulo> get(ArticuloId id);

    List<Articulo> searchAll();

    List<Articulo> matching(Criteria criteria);

    Long count(Criteria criteria);

}
