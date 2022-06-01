package com.rallibau.boundedContext.articulo.application.find;

import com.rallibau.boundedContext.articulo.domain.Articulo;
import com.rallibau.boundedContext.articulo.domain.ArticuloJpaRepository;
import com.rallibau.boundedContext.articulo.domain.ArticuloMother;
import com.rallibau.boundedContext.articulo.domain.ArticuloRepository;
import com.rallibau.shared.domain.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArticuloResponseFinderShould {
    private ArticuloFinder finder;
    private ArticuloRepository repository;
    private ArticuloJpaRepository repositoryJpa;


    @BeforeEach
    private void setUp() {
        repository = mock(ArticuloRepository.class);
        repositoryJpa = mock(ArticuloJpaRepository.class);
        finder = new ArticuloFinder(repository, repositoryJpa);
    }


    @Test
    public void find_by_id_dont_failure() {
        Articulo drug = ArticuloMother.random();
        when(repository.get(any())).thenReturn(Optional.of(ArticuloMother.random()));
        assertThat("we find a drug", finder.find(drug.id().value()) != null);
    }

    @Test
    public void find_by_id_and_guh_dont_failure() {
        Articulo drug = ArticuloMother.random();
        when(repository.get(any())).thenReturn(Optional.of(ArticuloMother.random()));
        assertThat("we find a drug", finder.find(drug.id().value()) != null);
    }



    @Test
    public void count_by_criteria() {
        Articulo drug = ArticuloMother.random();
        ArrayList<Articulo> articulos = new ArrayList<>();
        articulos.add(drug);
        when(repository.count(any())).thenReturn(Long.valueOf(articulos.size()));
        Filters filters = Filters.fromValues(Filter.create("name",
                FilterOperator.CONTAINS.value(), drug.name().value()));
        Criteria criteria = new Criteria(filters,
                Order.none(),
                Optional.empty(),
                Optional.empty());
        assertThat("we find 1", finder.count(criteria) == 1L);
    }
}
