package com.rallibau.boundedContext.articulo.infrastructure.persistence;

import com.rallibau.boundedContext.articulo.domain.Articulo;
import com.rallibau.boundedContext.articulo.domain.ArticuloMother;
import com.rallibau.boundedContext.articulo.infrastructure.persistence.inMemory.ArticuloRepositoryInMemoryImpl;
import com.rallibau.shared.domain.criteria.Criteria;
import com.rallibau.shared.domain.criteria.Filter;
import com.rallibau.shared.domain.criteria.FilterGroupOperator;
import com.rallibau.shared.domain.criteria.Filters;
import com.rallibau.shared.domain.criteria.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class ArticuloResponseRepositoryShould {

    private ArticuloRepositoryInMemoryImpl repository;

    @BeforeEach
    public void prepare() {
        repository = new ArticuloRepositoryInMemoryImpl();
    }


    @Test
    protected void save() {
        repository.save(ArticuloMother.random());
    }


    @Test
    public void search_all_existing() {
        Articulo drug1 = ArticuloMother.random();
        repository.save(drug1);
        Articulo drug2 = ArticuloMother.random();
        repository.save(drug2);
        assertThat(Arrays.asList(drug1, drug2), containsInAnyOrder(repository.searchAll().toArray()));
    }

    @Test
    public void search_by_criteria() {
        Articulo articulo = ArticuloMother.random();
        repository.save(articulo);
        Criteria criteria = new Criteria(
                new Filters(
                        Collections.singletonList(
                                Filter.create("name",
                                        "contains",
                                        articulo.name().value())),
                        FilterGroupOperator.OR),
                Order.asc("name"));
        assertThat(Collections.singletonList(articulo),
                containsInAnyOrder(repository.matching(criteria).toArray()));

    }
}
