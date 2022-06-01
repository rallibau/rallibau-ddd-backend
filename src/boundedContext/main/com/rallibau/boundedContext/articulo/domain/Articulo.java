package com.rallibau.boundedContext.articulo.domain;

import com.rallibau.shared.domain.AggregateRoot;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity(name = "present")
@Where(clause = "status != 0")
public final class Articulo extends AggregateRoot {

    @Id
    @EmbeddedId
    @AttributeOverrides(@AttributeOverride(name = "value", column = @Column(name = "codpres")))
    private final ArticuloId id;


    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "value", column = @Column(name = "nomregis")))
    private final ArticuloName name;


    public Articulo() {
        this.id = null;
        this.name = null;
    }

    public Articulo(ArticuloId id,
                    ArticuloName name) {
        this.id = id;
        this.name = name;
    }


    public static Articulo create(ArticuloId id,
                                  ArticuloName name) {
        return new Articulo(id, name);
    }

    public ArticuloId id() {

        return id;
    }


    public ArticuloName name() {

        return name;
    }

}
