package com.rallibau.shared.domain.criteria;

public class FilterGroup {
    private final Filter firstFilter;
    private final Filter secondFilter;
    private final FilterGroupOperator filterGroupOperator;

    public FilterGroup(Filter firstFilter, Filter secondFilter, FilterGroupOperator filterGroupOperator) {
        this.firstFilter = firstFilter;
        this.secondFilter = secondFilter;
        this.filterGroupOperator = filterGroupOperator;
    }

    public Filter firstFilter() {
        return firstFilter;
    }

    public Filter secondFilter() {
        return secondFilter;
    }

    public FilterGroupOperator filterGroupOperator() {
        return filterGroupOperator;
    }
}
