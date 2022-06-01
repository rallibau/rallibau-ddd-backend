package com.rallibau.shared.domain;

import javax.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public abstract class DoubleValueObject {
    private Double value;

    public DoubleValueObject(Double value) {
        this.value = value;
    }

    public Double value() {
        return value;
    }

    public Double getValue() {
        return value;
    }

    public void value(Double value) {
        this.value = value;
    }

    public DoubleValueObject() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DoubleValueObject that = (DoubleValueObject) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
