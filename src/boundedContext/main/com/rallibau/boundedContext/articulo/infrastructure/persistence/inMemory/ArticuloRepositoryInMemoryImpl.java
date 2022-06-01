package com.rallibau.boundedContext.articulo.infrastructure.persistence.inMemory;

import com.rallibau.boundedContext.articulo.domain.Articulo;
import com.rallibau.boundedContext.articulo.domain.ArticuloId;
import com.rallibau.boundedContext.articulo.domain.ArticuloRepository;
import com.rallibau.shared.domain.criteria.Criteria;
import com.rallibau.shared.infrastructure.persistence.memory.MemoryRepository;

import java.util.List;
import java.util.Optional;


public class ArticuloRepositoryInMemoryImpl extends MemoryRepository<Articulo, ArticuloId> implements ArticuloRepository {

    public ArticuloRepositoryInMemoryImpl() {
        super(Articulo.class);
    }

    @Override
    public void save(Articulo articulo) {
        persist(articulo);
    }

    @Override
    public Optional<Articulo> get(ArticuloId id) {
        return byId(id);
    }


    @Override
    public List<Articulo> searchAll() {
        return all();
    }

    @Override
    public List<Articulo> matching(Criteria criteria) {
        return byCriteria(criteria);
    }

    @Override
    public Long count(Criteria criteria) {
        return Long.valueOf(countByCriteria(criteria));
    }
}
