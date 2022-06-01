package com.rallibau.shared.domain.criteria;

import com.rallibau.shared.domain.IntegerValueObject;

public final class FilterIntegerValue extends IntegerValueObject implements FilterValue<Integer> {
    public FilterIntegerValue(Integer value) {
        super(value);
    }
}
