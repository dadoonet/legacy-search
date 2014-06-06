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
    public Collection<Person> findWithCriterias(Collection<Criterion> criterions, Integer from, Integer size) throws Exception;

    /**
     * Count persons using criteria
     */
    public long countWithCriterias(Collection<Criterion> criterions) throws Exception;

    /**
     * Find persons with a google like search.
     */
    public Collection<Person> findLikeGoogle(String query, Integer from, Integer size) throws Exception;

    /**
     * Count persons with a google like search.
     */
    public long countLikeGoogle(String query) throws Exception;
}
