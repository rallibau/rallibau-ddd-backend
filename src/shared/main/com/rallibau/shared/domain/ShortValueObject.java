package com.rallibau.shared.domain;

import javax.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public abstract class ShortValueObject {
    private Short value;

    public ShortValueObject(Short value) {
        this.value = value;
    }

    public ShortValueObject() {
    }

    public Short value() {
        return value;
    }

    public Short getValue() {
        return value;
    }

    public void value(Short value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ShortValueObject that = (ShortValueObject) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
