package fr.pilato.demo.legacysearch.dao;

import fr.pilato.demo.legacysearch.domain.Address;
import fr.pilato.demo.legacysearch.domain.Marketing;
import fr.pilato.demo.legacysearch.domain.Person;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class HibernateUtils {
    final static Logger logger = LoggerFactory.getLogger(HibernateUtils.class);

    private static SessionFactory sessionFactory = null;

    private static SessionFactory buildSessionFactory() {
        try {
            logger.debug("Create hibernate session factory");
            return new Configuration()
                    .addAnnotatedClass(Person.class)
                    .addAnnotatedClass(Address.class)
                    .addAnnotatedClass(Marketing.class)
                    .configure().buildSessionFactory(
                        new StandardServiceRegistryBuilder().configure().build()
                    );
        }
        catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void beginTransaction() {
        // Create a transaction if no one is opened yet
        if (getSession().getTransaction() == null || !getSession().getTransaction().isActive()) {
            getSession().beginTransaction();
        }
    }

    public static void commitTransaction() {
        if (getSession().getTransaction() != null && getSession().getTransaction().isActive()) {
            getSession().getTransaction().commit();
        }
    }

    public static void rollbackTransaction() {
        getSession().getTransaction().rollback();
    }

    public static Session getSession() {
        return getSessionFactory().getCurrentSession();
    }

    public static <T> T get(Class<T> entityClass, Serializable id) {
        return (T) getSession().get(entityClass, id);
    }

    public static List<?> find(final String queryString, final Object... values) {
        Query queryObject = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.list();
    }

    public static <T> T merge(final T entity) {
        return (T) getSession().merge(entity);
    }

    public static void delete(final Object entity) {
        getSession().delete(entity);
    }

    public static <T> void deleteAll(Collection<T> entities) {
        for (Object entity : entities) {
            getSession().delete(entity);
        }
    }

    public void startUp() {
        HibernateUtils.getSessionFactory();
    }
    public void cleanUp() {
        HibernateUtils.getSessionFactory().close();
    }
}
