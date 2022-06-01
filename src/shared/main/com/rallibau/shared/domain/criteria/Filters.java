package com.rallibau.shared.domain.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class Filters {
    private final List<Filter> filters;
    private final FilterGroupOperator filterGroupOperator;

    public Filters(List<Filter> filters, FilterGroupOperator filterGroupOperator) {
        this.filters = filters;
        this.filterGroupOperator = filterGroupOperator;
    }

    public static Filters fromValues(List<HashMap<String, String>> filters,FilterGroupOperator filterGroupOperator) {
        return new Filters(filters.stream().map(Filter::fromValues).collect(Collectors.toList()), filterGroupOperator);
    }

    public static Filters fromValues(Filter... filtersArray) {
        return new Filters(new ArrayList<>(Arrays.asList(filtersArray)), FilterGroupOperator.AND);
    }

    public static Filters fromValues(FilterGroupOperator filterGroupOperator,Filter... filtersArray) {
        return new Filters(new ArrayList<>(Arrays.asList(filtersArray)), filterGroupOperator);
    }

    public static Filters none() {
        return new Filters(Collections.emptyList(), FilterGroupOperator.NONE);
    }

    public List<Filter> filters() {
        return filters;
    }

    public FilterGroupOperator filterGroupOperator() {
        return filterGroupOperator;
    }
}
