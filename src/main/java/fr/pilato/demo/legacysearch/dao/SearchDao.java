package fr.pilato.demo.legacysearch.dao;


import fr.pilato.demo.legacysearch.domain.Person;
import org.hibernate.criterion.Criterion;

import java.util.Collection;

/**
 * Search for Person DAO interface.
 */
public interface SearchDao {

    /**
     * Find person using criteria
     */
    Collection<Person> findWithCriterias(Collection<Criterion> criterions, Integer from, Integer size);

    /**
     * Count persons using criteria
     */
    long countWithCriterias(Collection<Criterion> criterions);

    /**
     * Find persons with a google like search.
     */
    Collection<Person> findLikeGoogle(String query, Integer from, Integer size);

    /**
     * Count persons with a google like search.
     */
    long countLikeGoogle(String query);
}
