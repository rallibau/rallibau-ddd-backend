package com.rallibau.boundedContext.articulo.application.find;

import com.rallibau.shared.domain.bus.query.Response;
import lombok.Data;

@Data
public class ArticuloResponse implements Response {


    public String id;
    public String nombre;


    public ArticuloResponse(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;

    }

}
