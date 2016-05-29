package fr.pilato.demo.legacysearch.dao;


import fr.pilato.demo.legacysearch.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import java.util.Collection;
import java.util.List;

/**
 * Person DAO implementation.
 */
@Component
public class PersonDaoImpl implements PersonDao {
    final Logger logger = LoggerFactory.getLogger(PersonDaoImpl.class);

    private final HibernateService hibernateService;

    public PersonDaoImpl(HibernateService hibernateService) {
        this.hibernateService = hibernateService;
    }

    /**
     * Get Person
     * @param id
     * @return Person
     */
    public Person get(Integer id) {
        logger.debug("get({})", id);
        return hibernateService.get(Person.class, id);
    }

    /**
     * Find person by reference.
     */
    public Person getByReference(String reference) {
        List persons = hibernateService.find("from Person p where p.reference = ?", reference);

        if (persons.size() == 0) return null;

        if (persons.size() > 1) {
            logger.warn("we have {} persons for reference {}. Getting the first one...", persons.size(), reference);
        }

        return (Person) persons.get(0);
    }

    /**
     * Saves person.
     */
    public Person save(Person person) {
        if (logger.isTraceEnabled()) logger.trace("save({})", person);
        return hibernateService.merge(person);
    }

    /**
     * Delete person.
     */
    public void delete(Person person) {
        logger.debug("delete({})", person);
        hibernateService.delete(person);
    }

    /**
     * Delete persons.
     */
    public void deleteAll(Collection<Person> persons) {
        hibernateService.deleteAll(persons);
    }
}
