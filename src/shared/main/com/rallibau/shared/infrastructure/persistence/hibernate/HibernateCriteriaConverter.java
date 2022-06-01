package com.rallibau.shared.infrastructure.persistence.hibernate;

import com.rallibau.shared.domain.criteria.Criteria;
import com.rallibau.shared.domain.criteria.Filter;
import com.rallibau.shared.domain.criteria.FilterDateValue;
import com.rallibau.shared.domain.criteria.FilterGroupOperator;
import com.rallibau.shared.domain.criteria.FilterIntegerValue;
import com.rallibau.shared.domain.criteria.FilterOperator;
import com.rallibau.shared.domain.criteria.FilterStringValue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class HibernateCriteriaConverter<T> {
    private final CriteriaBuilder builder;
    private final HashMap<FilterOperator, BiFunction<Filter, Root<T>, Predicate>> predicateTransformers = new HashMap<FilterOperator, BiFunction<Filter, Root<T>, Predicate>>() {{
        put(FilterOperator.EQUAL, HibernateCriteriaConverter.this::equalsPredicateTransformer);
        put(FilterOperator.NOT_EQUAL, HibernateCriteriaConverter.this::notEqualsPredicateTransformer);
        put(FilterOperator.GT, HibernateCriteriaConverter.this::greaterThanPredicateTransformer);
        put(FilterOperator.LT, HibernateCriteriaConverter.this::lowerThanPredicateTransformer);
        put(FilterOperator.CONTAINS, HibernateCriteriaConverter.this::containsPredicateTransformer);
        put(FilterOperator.NOT_CONTAINS, HibernateCriteriaConverter.this::notContainsPredicateTransformer);
    }};

    public HibernateCriteriaConverter(CriteriaBuilder builder) {
        this.builder = builder;
    }

    public CriteriaQuery<T> convert(Criteria criteria, Class<T> aggregateClass) {
        CriteriaQuery<T> hibernateCriteria = builder.createQuery(aggregateClass);
        Root<T> root = hibernateCriteria.from(aggregateClass);

        if (criteria.filters().filters().size() == 1) {
            hibernateCriteria.where(formatPredicates(criteria.filters().filters(), root));
        } else if (criteria.filters().filters().size() > 1) {
            if (criteria.filters().filterGroupOperator().equals(FilterGroupOperator.OR)) {
                hibernateCriteria.where(builder.or(formatPredicates(criteria.filters().filters(), root)));
            }
            if (criteria.filters().filterGroupOperator().equals(FilterGroupOperator.AND)) {
                hibernateCriteria.where(builder.and(formatPredicates(criteria.filters().filters(), root)));
            }
        }


        if (criteria.order().hasOrder()) {
            Path<Object> orderBy = root.get(criteria.order().orderBy().value());
            Order order = criteria.order().orderType().isAsc() ? builder.asc(orderBy) : builder.desc(orderBy);

            hibernateCriteria.orderBy(order);
        }

        return hibernateCriteria;
    }

    public CriteriaQuery<Long> convertToCount(Criteria criteria, Class<T> aggregateClass) {
        CriteriaQuery<Long> hibernateCriteria = builder.createQuery(Long.class);
        Root<T> root = hibernateCriteria.from(aggregateClass);

        if (criteria.filters().filters().size() == 1) {
            hibernateCriteria.where(formatPredicates(criteria.filters().filters(), root));
        } else if (criteria.filters().filters().size() > 1) {
            if (criteria.filters().filterGroupOperator().equals(FilterGroupOperator.OR)) {
                hibernateCriteria.where(builder.or(formatPredicates(criteria.filters().filters(), root)));
            }
            if (criteria.filters().filterGroupOperator().equals(FilterGroupOperator.AND)) {
                hibernateCriteria.where(builder.and(formatPredicates(criteria.filters().filters(), root)));
            }
        }

        return hibernateCriteria.select(builder.count(root));
    }

    private Predicate[] formatPredicates(List<Filter> filters, Root<T> root) {
        List<Predicate> predicates = filters.stream().map(filter -> formatPredicate(filter, root)).collect(Collectors.toList());

        Predicate[] predicatesArray = new Predicate[predicates.size()];
        predicatesArray = predicates.toArray(predicatesArray);

        return predicatesArray;
    }

    private Predicate formatPredicate(Filter filter, Root<T> root) {
        BiFunction<Filter, Root<T>, Predicate> transformer = predicateTransformers.get(filter.operator());

        return transformer.apply(filter, root);
    }

    private Predicate equalsPredicateTransformer(Filter filter, Root<T> root) {
        return builder.equal(root.get(filter.field().value()).get("value"), filter.value().value());
    }

    private Predicate notEqualsPredicateTransformer(Filter filter, Root<T> root) {
        return builder.notEqual(root.get(filter.field().value()).get("value"), filter.value().value());
    }

    private Predicate greaterThanPredicateTransformer(Filter filter, Root<T> root) {
        if(filter.value() instanceof FilterStringValue){
            return builder.greaterThan(root.get(filter.field().value()).get("value"), (String)filter.value().value());
        }
        if(filter.value() instanceof FilterDateValue){
            return builder.greaterThan(root.get(filter.field().value()).get("value"), (Date)filter.value().value());
        }
        if(filter.value() instanceof FilterIntegerValue){
            return builder.greaterThan(root.get(filter.field().value()).get("value"), (Integer)filter.value().value());
        }
        throw new UnsupportedOperationException();
    }

    private Predicate lowerThanPredicateTransformer(Filter filter, Root<T> root) {
        if(filter.value() instanceof FilterStringValue){
            return builder.lessThan(root.get(filter.field().value()).get("value"), (String)filter.value().value());
        }
        if(filter.value() instanceof FilterDateValue){
            return builder.lessThan(root.get(filter.field().value()).get("value"), (Date)filter.value().value());
        }
        if(filter.value() instanceof FilterIntegerValue){
            return builder.lessThan(root.get(filter.field().value()).get("value"), (Integer)filter.value().value());
        }
        throw new UnsupportedOperationException();
    }

    private Predicate containsPredicateTransformer(Filter filter, Root<T> root) {
        return builder.like(root.get(filter.field().value()).get("value"), String.format("%%%s%%", filter.value().value()));
    }

    private Predicate notContainsPredicateTransformer(Filter filter, Root<T> root) {
        return builder.notLike(root.get(filter.field().value()).get("value"), String.format("%%%s%%", filter.value().value()));
    }
}
