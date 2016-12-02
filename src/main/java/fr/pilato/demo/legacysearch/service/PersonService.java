package fr.pilato.demo.legacysearch.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.demo.legacysearch.dao.HibernateService;
import fr.pilato.demo.legacysearch.dao.PersonDao;
import fr.pilato.demo.legacysearch.dao.SearchDao;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.helper.PersonGenerator;
import fr.pilato.demo.legacysearch.webapp.InitResult;
import org.dozer.DozerBeanMapper;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PersonService {
    final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonDao personDao;
    private final SearchDao searchDao;
    private final HibernateService hibernateService;
    private final ObjectMapper mapper;
    private final DozerBeanMapper dozerBeanMapper;

    @Inject
    public PersonService(PersonDao personDao, SearchDao searchDao,
                         HibernateService hibernateService,
                         ObjectMapper mapper, DozerBeanMapper dozerBeanMapper) {
        this.personDao = personDao;
        this.searchDao = searchDao;
        this.hibernateService = hibernateService;
        this.mapper = mapper;
        this.dozerBeanMapper = dozerBeanMapper;
    }

    public Person get(Integer id) {
        hibernateService.beginTransaction();

        Person person = personDao.get(id);
        logger.debug("get({})={}", id, person);

        hibernateService.commitTransaction();
        return person;
    }

    public Person save(Person person) {
        hibernateService.beginTransaction();

        Person personDb = personDao.save(person);

        hibernateService.commitTransaction();
        return personDb;
    }

    public Person upsert(Integer id, Person person) {
        // We try to find an existing document
        Person personDb = get(id);
        if (personDb != null) {
            dozerBeanMapper.map(person, personDb);
            person = personDb;
            person.setId(id);
        }
        person = save(person);

        return person;
    }

    public boolean delete(Integer id) {
        logger.debug("Person: {}", id);

        if (id == null) {
            return false;
        }

        hibernateService.beginTransaction();
        Person person = personDao.get(id);
        if (person == null) {
            logger.debug("Person with reference {} does not exist", id);
            hibernateService.commitTransaction();
            return false;
        }
        personDao.delete(person);
        hibernateService.commitTransaction();

        logger.debug("Person deleted: {}", id);

        return true;
    }

    public String search(String q, String f_country, String f_date, Integer from, Integer size) {
        long start = System.currentTimeMillis();

        hibernateService.beginTransaction();
        long total = searchDao.countLikeGoogle(q);
        Collection<Person> personsFound = searchDao.findLikeGoogle(q, from, size);
        hibernateService.commitTransaction();
        long took = System.currentTimeMillis() - start;

        RestSearchResponse<Person> response = buildResponse(personsFound, total, took);

        logger.debug("search({})={} persons", q, response.getHits().getTotalHits());

        String json = null;
        try {
            json = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("can not serialize to json", e);
        }

        return json;
    }

    public String advancedSearch(String name, String country, String city, Integer from, Integer size) {
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

        hibernateService.beginTransaction();
        long total = searchDao.countWithCriterias(criterions);
        Collection<Person> personsFound = searchDao.findWithCriterias(criterions, from, size);
        hibernateService.commitTransaction();
        long took = System.currentTimeMillis() - start;

        RestSearchResponse<Person> response = buildResponse(personsFound, total, took);

        logger.debug("advancedSearch({},{},{})={} persons", name, country, city, response.getHits().getTotalHits());

        String json = null;
        try {
            json = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("can not serialize to json", e);
        }

        return json;
    }

    private AtomicInteger currentItem = new AtomicInteger();
    private long start = 0;

    public InitResult init(Integer size) {
        currentItem.set(0);

        logger.debug("Initializing database for {} persons", size);
        start = System.currentTimeMillis();

        try {
            hibernateService.beginTransaction();
            Person joe = PersonGenerator.personGenerator();
            joe.setName("Joe Smith");
            joe.getAddress().setCountry("France");
            joe.getAddress().setCity("Paris");
            save(joe);
            currentItem.incrementAndGet();

            Person franceGall = PersonGenerator.personGenerator();
            franceGall.setName("France Gall");
            franceGall.setGender("female");
            franceGall.getAddress().setCountry("Italy");
            franceGall.getAddress().setCity("Ischia");
            save(franceGall);
            currentItem.incrementAndGet();

            // We generate numPersons persons
            for (int i = 2; i < size; i++) {
                Person person = PersonGenerator.personGenerator();
                save(person);
                currentItem.incrementAndGet();
            }
        } catch (IOException e) {
            logger.warn("error while generating data", e);
        } finally {
            hibernateService.commitTransaction();
        }

        long took = System.currentTimeMillis() - start;

        logger.debug("Database initialized with {} persons. Took: {} ms, around {} per second.",
                size, took, 1000 * size / took);

        return new InitResult(took, 1000 * size / took, size);
    }

    public InitResult getInitCurrentAchievement() {
        int current = currentItem.get();
        long took = System.currentTimeMillis() - start;
        return new InitResult(took, 1000 * current / took, current);
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
            hit.set_id(person.idAsString());
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
        private String _id;

        public void set_source(T _source) {
            this._source = _source;
        }

        public T get_source() {
            return _source;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String get_id() {
            return _id;
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
