package com.rallibau.shared.domain;

import javax.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public abstract class IntegerValueObject {
    private Integer value;

    public IntegerValueObject(Integer value) {
        this.value = value;
    }

    public Integer value() {
        return value;
    }

    public Integer getValue() {
        return value;
    }

    public void value(Integer value) {
        this.value = value;
    }

    public IntegerValueObject(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntegerValueObject that = (IntegerValueObject) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
