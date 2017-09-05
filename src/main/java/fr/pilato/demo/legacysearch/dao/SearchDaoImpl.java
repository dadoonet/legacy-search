package fr.pilato.demo.legacysearch.dao;


import fr.pilato.demo.legacysearch.domain.Person;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import java.util.Collection;


/**
 * Person DAO implementation.
 * 
 * @author David Pilato
 */
@Component
public class SearchDaoImpl implements SearchDao {
    final Logger logger = LoggerFactory.getLogger(SearchDaoImpl.class);

    private final HibernateService hibernateService;

    public SearchDaoImpl(HibernateService hibernateService) {
        this.hibernateService = hibernateService;
    }

    /**
     * Find persons by any column (like full text).
     */
    @SuppressWarnings("unchecked")
    public Collection<Person> findLikeGoogle(String query, Integer from, Integer size) {
        Criteria criteria = generateQuery(hibernateService.getSession(), Person.class, query);
        criteria.setFirstResult(from);
        criteria.setMaxResults(size);
        return criteria.list();
    }

    /**
     * Generate a google like query
     */
    @Override
    public long countLikeGoogle(String query) {
        int size = generateQuery(hibernateService.getSession(), Person.class, query).list().size();
        return size;
    }

    /**
     * Find persons by criterias.
     */
    @SuppressWarnings("unchecked")
    public Collection<Person> findWithCriterias(Collection<Criterion> criterions, Integer from, Integer size) {
        Criteria criteria = hibernateService.getSession().createCriteria(Person.class);
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
    public long countWithCriterias(Collection<Criterion> criterions) {
        Criteria criteria = hibernateService.getSession().createCriteria(Person.class);
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

        return c;
    }
}
