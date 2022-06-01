package com.rallibau.boundedContext.articulo.application.find;

import com.rallibau.boundedContext.articulo.domain.Articulo;
import com.rallibau.boundedContext.articulo.domain.ArticuloId;
import com.rallibau.boundedContext.articulo.domain.ArticuloJpaRepository;
import com.rallibau.boundedContext.articulo.domain.ArticuloNotFound;
import com.rallibau.boundedContext.articulo.domain.ArticuloRepository;
import com.rallibau.shared.domain.Service;
import com.rallibau.shared.domain.criteria.Criteria;

import java.util.List;

@Service
public class ArticuloFinder {
    private final ArticuloRepository repository;
    private final ArticuloJpaRepository repositoryJpa;


    public ArticuloFinder(ArticuloRepository repository, ArticuloJpaRepository repositoryJpa) {
        this.repository = repository;
        this.repositoryJpa = repositoryJpa;
    }


    public Articulo find(String id) {

        return repository.get(ArticuloId.create(id)).orElseThrow(() -> new ArticuloNotFound(ArticuloId.create(id)));
    }


    public List<Articulo> findJpa(Criteria criteria, String guh) {
        return repositoryJpa.findAll();
    }

    public Long count(Criteria criteria) {
        return repository.count(criteria);
    }


}
