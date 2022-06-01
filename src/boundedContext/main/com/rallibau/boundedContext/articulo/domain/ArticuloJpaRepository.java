package com.rallibau.boundedContext.articulo.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticuloJpaRepository extends JpaRepository<Articulo,ArticuloId> {

}
