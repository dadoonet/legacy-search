package fr.pilato.demo.legacysearch.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.util.List;

@Service
public class HibernateService {
    private final Logger logger = LoggerFactory.getLogger(HibernateService.class);

    private SessionFactory sessionFactory;

    @Autowired
    public HibernateService(EntityManagerFactory factory) {
        if(factory.unwrap(SessionFactory.class) == null){
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.sessionFactory = factory.unwrap(SessionFactory.class);
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
}
