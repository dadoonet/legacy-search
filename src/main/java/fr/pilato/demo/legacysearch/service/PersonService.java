package fr.pilato.demo.legacysearch.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.demo.legacysearch.dao.*;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.helper.PersonGenerator;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PersonService {
    final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonDao personDao;
    private final SearchDao searchDao;
    private final ObjectMapper mapper;

    public PersonService() {
        personDao = new PersonDaoImpl();
        searchDao = new SearchDaoImpl();
        mapper = new ObjectMapper();
    }

    public Person get(String id) throws Exception {
        HibernateUtils.beginTransaction();

        Person person = personDao.getByReference(id);
        if (logger.isDebugEnabled()) logger.debug("get({})={}", id, person);

        HibernateUtils.commitTransaction();
        return person;
    }

    public Person save(Person person) {
        HibernateUtils.beginTransaction();

        Person personDb = personDao.save(person);

        HibernateUtils.commitTransaction();
        return personDb;
    }

    public boolean delete(String id) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("Person: {}", id);

        if (id == null) {
            return false;
        }

        HibernateUtils.beginTransaction();
        Person person = get(id);
        if (person == null) {
            if (logger.isDebugEnabled()) logger.debug("Person with reference {} does not exist", id);
            HibernateUtils.commitTransaction();
            return false;
        }
        personDao.delete(person);
        HibernateUtils.commitTransaction();

        if (logger.isDebugEnabled()) logger.debug("Person deleted: {}", id);

        return true;
    }

    public String search(String q, String f_country, String f_date, Integer from, Integer size) throws Exception {
        long start = System.currentTimeMillis();

        HibernateUtils.beginTransaction();
        long total = searchDao.countLikeGoogle(q);
        Collection<Person> personsFound = searchDao.findLikeGoogle(q, from, size);
        HibernateUtils.commitTransaction();
        long took = System.currentTimeMillis() - start;

        RestSearchResponse<Person> response = buildResponse(personsFound, total, took);

        if (logger.isDebugEnabled()) logger.debug("search({})={} persons", q, response.getHits().getTotalHits());

        return mapper.writeValueAsString(response);
    }

    public String advancedSearch(String name, String country, String city, Integer from, Integer size) throws Exception {
        List<Criterion> criterions = new ArrayList<>();
        if (name != null) {
            criterions.add(Restrictions.ilike("name", "%" + name + "%"));
        }
        if (country != null) {
            criterions.add(Restrictions.ilike("address.country", "%" + country + "%"));
        }
        if (city != null) {
            criterions.add(Restrictions.ilike("address.city", "%" + city + "%"));
        }

        long start = System.currentTimeMillis();

        HibernateUtils.beginTransaction();
        long total = searchDao.countWithCriterias(criterions);
        Collection<Person> personsFound = searchDao.findWithCriterias(criterions, from, size);
        HibernateUtils.commitTransaction();
        long took = System.currentTimeMillis() - start;

        RestSearchResponse<Person> response = buildResponse(personsFound, total, took);

        if (logger.isDebugEnabled()) logger.debug("advancedSearch({},{},{})={} persons", name, country, city, response.getHits().getTotalHits());

        return mapper.writeValueAsString(response);
    }

    public boolean init(Integer size) throws IOException {
        if (logger.isDebugEnabled()) logger.debug("Initializing database for {} persons", size);

        long start = System.currentTimeMillis();

        HibernateUtils.beginTransaction();
        Person joe = PersonGenerator.personGenerator();
        joe.setName("Joe Smith");
        joe.setReference("0");
        save(joe);

        // We generate numPersons persons
        for (int i = 1; i < size; i++) {
            Person person = PersonGenerator.personGenerator();
            person.setReference("" + i);
            save(person);
        }
        HibernateUtils.commitTransaction();

        long took = System.currentTimeMillis() - start;

        if (logger.isDebugEnabled()) logger.debug("Database initialized with {} persons. Took: {} ms, around {} per second.",
                size, took, 1000 * size / took);

        return true;
    }

    private RestSearchResponse<Person> buildResponse(Collection<Person> persons, long total, long took) {
        RestSearchResponse<Person> response = new RestSearchResponse<>();
        response.setTook(took);

        RestSearchHits<Person> hits = new RestSearchHits<>();
        RestSearchHit<Person>[] hitsItems = new RestSearchHit[persons.size()];

        int i =0;
        for (Person person : persons) {
            RestSearchHit hit = new RestSearchHit();
            hit.set_source(person);
            hitsItems[i++] = hit;
        }
        hits.setHits(hitsItems).setTotal(total);
        response.setHits(hits);

        return response;
    }

    public static class RestSearchHits<T> {
        private RestSearchHit<T>[] hits;
        private long total;

        public long getTotal() {
            return total;
        }

        // Just for elasticsearch compatibility purpose
        @JsonIgnore
        public long getTotalHits() {
            return total;
        }

        public RestSearchHits<T> setTotal(long total) {
            this.total = total;
            return this;
        }

        public RestSearchHit<T>[] getHits() {
            return hits;
        }

        public RestSearchHits<T> setHits(RestSearchHit<T>[] hits) {
            this.hits = hits;
            return this;
        }
    }

    public static class RestSearchHit<T> {
        private T _source;

        public void set_source(T _source) {
            this._source = _source;
        }

        public T get_source() {
            return _source;
        }
    }

    public static class RestSearchResponse<T> {
        private long took;
        private RestSearchHits hits;

        public RestSearchResponse() {
        }

        public RestSearchHits getHits() {
            return hits;
        }

        public void setHits(RestSearchHits hits) {
            this.hits = hits;
        }

        public long getTook() {
            return took;
        }

        public RestSearchResponse setTook(long took) {
            this.took = took;
            return this;
        }
    }
}
