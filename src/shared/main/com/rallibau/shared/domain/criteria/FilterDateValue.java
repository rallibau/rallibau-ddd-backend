package com.rallibau.shared.domain.criteria;

import com.rallibau.shared.domain.DateValueObject;

import java.util.Date;

public final class FilterDateValue extends DateValueObject implements FilterValue<Date> {
    public FilterDateValue(Date value) {
        super(value);
    }
}
