package fr.pilato.demo.legacysearch.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Component
public class HibernateService {
    private final Logger logger = LoggerFactory.getLogger(HibernateService.class);

    private final SessionFactory sessionFactory;

    @Inject
    public HibernateService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void beginTransaction() {
        // Create a transaction if no one is opened yet
        if (getSession().getTransaction() == null || getSession().getTransaction().getStatus().isOneOf(TransactionStatus.NOT_ACTIVE)) {
            getSession().beginTransaction();
        }
    }

    public void commitTransaction() {
        if (getSession().getTransaction() != null && getSession().getTransaction().getStatus().isOneOf(TransactionStatus.ACTIVE)) {
            getSession().getTransaction().commit();
        }
    }

    public void rollbackTransaction() {
        getSession().getTransaction().rollback();
    }

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public <T> T get(Class<T> entityClass, Serializable id) {
        return (T) getSession().get(entityClass, id);
    }

    public List<?> find(final String queryString, final Object... values) {
        Query queryObject = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.list();
    }

    public <T> T merge(final T entity) {
        return (T) getSession().merge(entity);
    }

    public void delete(final Object entity) {
        getSession().delete(entity);
    }

    public <T> void deleteAll(Collection<T> entities) {
        for (Object entity : entities) {
            getSession().delete(entity);
        }
    }
}
