package fr.pilato.demo.legacysearch.dao;


import fr.pilato.demo.legacysearch.domain.Person;

import java.util.Collection;


/**
 * Person DAO interface for CRUD methods.
 */
public interface PersonDao {
    /**
     * Get person.
     */
    public Person get(Integer id);

    /**
     * Saves person.
     */
    public Person save(Person person);

    /**
     * Delete person.
     */
    public void delete(Person person);

    /**
     * Delete persons.
     */
    public void deleteAll(Collection<Person> persons);


}
