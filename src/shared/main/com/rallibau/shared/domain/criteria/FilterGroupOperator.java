package com.rallibau.shared.domain.criteria;

public enum FilterGroupOperator {
    OR("OR"),
    AND("AND"),
    NONE("");

    private final String operator;

    FilterGroupOperator(String operator) {
        this.operator = operator;
    }


    public String value() {
        return operator;
    }
    }
