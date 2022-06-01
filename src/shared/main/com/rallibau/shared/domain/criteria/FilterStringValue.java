package com.rallibau.shared.domain.criteria;

import com.rallibau.shared.domain.StringValueObject;

public final class FilterStringValue extends StringValueObject implements FilterValue<String>{
    public FilterStringValue(String value) {
        super(value);
    }
}
