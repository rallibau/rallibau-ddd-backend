package com.rallibau.shared.infrastructure.persistence.hibernate;

import com.rallibau.shared.domain.Identifier;
import com.rallibau.shared.domain.AggregateRoot;
import com.rallibau.shared.domain.criteria.Criteria;
import org.hibernate.SessionFactory;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public abstract class HibernateRepository<T extends AggregateRoot, J extends Identifier> {
    protected final SessionFactory sessionFactory;
    protected final Class<T> aggregateClass;
    protected final HibernateCriteriaConverter<T> criteriaConverter;

    public HibernateRepository(SessionFactory sessionFactory, Class<T> aggregateClass) {
        this.sessionFactory = sessionFactory;
        this.aggregateClass = aggregateClass;
        this.criteriaConverter = new HibernateCriteriaConverter<>(sessionFactory.getCriteriaBuilder());
    }

    protected void persist(T entity) {
        sessionFactory.getCurrentSession().saveOrUpdate(entity);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();


    }

    protected Optional<T> byId(J id) {
        return Optional.ofNullable(sessionFactory.getCurrentSession().byId(aggregateClass).load(id));
    }

    protected List<T> byCriteria(Criteria criteria) {
        CriteriaQuery<T> hibernateCriteria = criteriaConverter.convert(criteria, aggregateClass);

        Query query = sessionFactory.getCurrentSession().createQuery(hibernateCriteria);
        if(!criteria.limit().equals(Optional.empty())){
            query.setMaxResults(criteria.limit().get());
        }
        if(!criteria.offset().equals(Optional.empty())){
            query.setFirstResult(criteria.offset().get());
        }



        return query.getResultList();
    }

    public Long countByCriteria(Criteria criteria) {
        CriteriaQuery<Long> hibernateCriteria = criteriaConverter.convertToCount(criteria, aggregateClass);
        Query query = sessionFactory.getCurrentSession().createQuery(hibernateCriteria);

        return (Long) query.getSingleResult();
    }

    protected List<T> all() {
        CriteriaQuery<T> criteria = sessionFactory.getCriteriaBuilder().createQuery(aggregateClass);

        criteria.from(aggregateClass);

        return sessionFactory.getCurrentSession().createQuery(criteria).getResultList();
    }

    public BigInteger getNextKey(String sequenceName){
        Query query = sessionFactory.getCurrentSession().createSQLQuery("SELECT NEXT VALUE FOR "+sequenceName+" FROM RDB$DATABASE");
        return  (BigInteger) query.getSingleResult();

    }
}
