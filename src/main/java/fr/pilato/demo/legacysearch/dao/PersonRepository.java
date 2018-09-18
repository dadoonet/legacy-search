package fr.pilato.demo.legacysearch.dao;


import fr.pilato.demo.legacysearch.domain.Person;
import org.springframework.data.repository.CrudRepository;


/**
 * Person DAO interface for CRUD methods.
 */
public interface PersonRepository extends CrudRepository<Person, Integer> {

}

