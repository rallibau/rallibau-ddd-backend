package com.rallibau.boundedContext.articulo.infrastructure.persistence.hibernate;

import com.rallibau.boundedContext.articulo.domain.Articulo;
import com.rallibau.boundedContext.articulo.domain.ArticuloId;
import com.rallibau.boundedContext.articulo.domain.ArticuloRepository;
import com.rallibau.shared.domain.Service;
import com.rallibau.shared.domain.criteria.Criteria;
import com.rallibau.shared.infrastructure.persistence.hibernate.HibernateRepository;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional("presentaciones-transaction_manager")
public class ArticuloRepositoryImpl extends HibernateRepository<Articulo, ArticuloId> implements ArticuloRepository {

    public ArticuloRepositoryImpl(@Qualifier("presentaciones-session_factory") SessionFactory sessionFactory) {
        super(sessionFactory, Articulo.class);
    }

    @Override
    public void save(Articulo articulo) {
        persist(articulo);
    }

    @Override
    public Optional<Articulo> get(ArticuloId id) {

        return byId(id);
    }


    @Override
    public List<Articulo> searchAll() {
        return all();
    }

    @Override
    public List<Articulo> matching(Criteria criteria) {
        return byCriteria(criteria);
    }

    @Override
    public Long count(Criteria criteria) {
        return countByCriteria(criteria);
    }


}
