package fr.pilato.demo.legacysearch.dao;


import fr.pilato.demo.legacysearch.domain.Person;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;


/**
 * Person DAO implementation.
 * 
 * @author David Pilato
 */
@Repository
@Transactional
public class SearchDaoImpl implements SearchDao {
    final Logger logger = LoggerFactory.getLogger(SearchDaoImpl.class);

    protected HibernateTemplate template = null;

    /**
     * Sets Hibernate session factory.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        template = new HibernateTemplate(sessionFactory);
    }
    
    /**
     * Find persons by any column (like full text).
     */
    @SuppressWarnings("unchecked")
    public Collection<Person> findLikeGoogle(String query, Integer from, Integer size) throws DataAccessException {
        Session session = SessionFactoryUtils.getSession(template.getSessionFactory(), true);

        Criteria criteria = generateQuery(session, Person.class, query);
        criteria.setFirstResult(from);
        criteria.setMaxResults(size);

        return criteria.list();
    }

    /**
     * Generate a google like query
     */
    @Override
    public long countLikeGoogle(String query) throws Exception {
        Session session = SessionFactoryUtils.getSession(template.getSessionFactory(), true);
        return generateQuery(session, Person.class, query).list().size();
    }

    /**
     * Find persons by criterias.
     */
    @SuppressWarnings("unchecked")
    public Collection<Person> findWithCriterias(Collection<Criterion> criterions, Integer from, Integer size) throws Exception {
        Session session = SessionFactoryUtils.getSession(template.getSessionFactory(), true);

        Criteria criteria = session.createCriteria(Person.class);
        criteria.createAlias("address", "address");

        for (Criterion crit : criterions) {
            criteria.add(crit);
        }

        criteria.setFirstResult(from);
        criteria.setMaxResults(size);

        return criteria.list();
    }

    /**
     * Find persons by criterias.
     */
    @SuppressWarnings("unchecked")
    public long countWithCriterias(Collection<Criterion> criterions) throws Exception {
        Session session = SessionFactoryUtils.getSession(template.getSessionFactory(), true);

        Criteria criteria = session.createCriteria(Person.class);
        criteria.createAlias("address", "address");

        for (Criterion crit : criterions) {
            criteria.add(crit);
        }

        return criteria.list().size();
    }

    private Criteria generateQuery(Session session, Class clazz, String query) {
        String toLikeQuery = "%" + query + "%";
        Criteria c = session.createCriteria(clazz);
        c.createAlias("address", "address");

        c.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("name", toLikeQuery))
                        .add(Restrictions.ilike("address.country", toLikeQuery))
                        .add(Restrictions.ilike("address.city", toLikeQuery))
        );

        c.addOrder(Order.asc("name"));

        return c;
    }
}
