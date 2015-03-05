package fr.pilato.demo.legacysearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.demo.legacysearch.dao.*;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.helper.PersonGenerator;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PersonService {
    final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonDao personDao;
    private final SearchDao searchDao;
    private final ObjectMapper mapper;
    private final ElasticsearchDao elasticsearchDao;

    public PersonService() {
        personDao = new PersonDaoImpl();
        searchDao = new SearchDaoImpl();
        mapper = new ObjectMapper();
        elasticsearchDao = new ElasticsearchDao();
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
        try {
            elasticsearchDao.save(person);
        } catch (Exception e) {
            logger.error("Houston, we have a problem!", e);
        }

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
        elasticsearchDao.delete(person.getReference());
        HibernateUtils.commitTransaction();

        if (logger.isDebugEnabled()) logger.debug("Person deleted: {}", id);

        return true;
    }

    public String search(String q, String f_country, String f_date, Integer from, Integer size) throws Exception {
        QueryBuilder query;
        // If the user does not provide any text to query, let's match all documents
        if (!Strings.hasText(q)) {
            query = QueryBuilders.matchAllQuery();
        } else {
            query = QueryBuilders.simpleQueryString(q)
                    .field("name")
                    .field("gender")
                    .field("country")
                    .field("city");
        }

        SearchResponse response = elasticsearchDao.search(query, from, size);

        if (logger.isDebugEnabled()) logger.debug("search({})={} persons", q, response.getHits().getTotalHits());

        return response.toString();
    }

    public String advancedSearch(String name, String country, String city, Integer from, Integer size) throws Exception {
        QueryBuilder query;

        // If the user does not provide any text to query, let's match all documents
        if (!Strings.hasText(name) && !Strings.hasText(country) && !Strings.hasText(city)) {
            query = QueryBuilders.matchAllQuery();
        } else {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            if (Strings.hasText(name)) {
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery("name", name)
                );
            }
            if (Strings.hasText(country)) {
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery("address.country", country)
                );
            }
            if (Strings.hasText(city)) {
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery("address.city", city)
                );
            }

            query = boolQueryBuilder;
        }

        SearchResponse response = elasticsearchDao.search(query, from, size);

        if (logger.isDebugEnabled()) logger.debug("advancedSearch({},{},{})={} persons", name, country, city, response.getHits().getTotalHits());

        return response.toString();
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
}
