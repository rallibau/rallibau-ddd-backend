package com.rallibau.shared.domain;

import javax.persistence.MappedSuperclass;
import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Objects;

@MappedSuperclass
public abstract class BlobValueObject {
    private Blob value;

    public BlobValueObject(String value) throws SQLException {
        this.value = new SerialBlob(value.getBytes());
    }

    public String value() throws SQLException {
        return new String(value.getBytes(1, (int) value.length()));
    }

    public void value(String value) throws SQLException {
        this.value = new SerialBlob(value.getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlobValueObject)) {
            return false;
        }
        BlobValueObject that = (BlobValueObject) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
