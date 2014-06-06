package fr.pilato.demo.legacysearch.dao;


import fr.pilato.demo.legacysearch.domain.Person;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Person DAO implementation.
 */
@Repository
@Transactional
public class PersonDaoImpl implements PersonDao {
    final Logger logger = LoggerFactory.getLogger(PersonDaoImpl.class);

    protected HibernateTemplate template = null;

    /**
     * Sets Hibernate session factory.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        template = new HibernateTemplate(sessionFactory);
    }

    /**
     * Get Person
     * @param id
     * @return Person
     */
    public Person get(Integer id) {
        if (logger.isDebugEnabled()) logger.debug("get({})", id);

        return template.get(Person.class, id);
    }

    /**
     * Find person by reference.
     */
    public Person getByReference(String reference) throws DataAccessException {
        List persons = template.find("from Person p where p.reference = ?", reference);
        if (persons.size() == 0) return null;

        if (persons.size() > 1) {
            logger.warn("we have {} persons for reference {}. Getting the first one...", persons.size(), reference);
        }

        return (Person) persons.get(0);
    }

    /**
     * Saves person.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Person save(Person person) {
        if (logger.isTraceEnabled()) logger.trace("save({})", person);

        return template.merge(person);
    }

    /**
     * Delete person.
     */
    public void delete(Person person) {
        if (logger.isDebugEnabled()) logger.debug("delete({})", person);
        template.delete(person);
    }

    /**
     * Delete persons.
     */
    public void deleteAll(Collection<Person> persons) {
        template.deleteAll(persons);
    }
}
