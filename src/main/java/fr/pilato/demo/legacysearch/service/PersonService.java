package fr.pilato.demo.legacysearch.service;

import fr.pilato.demo.legacysearch.dao.ElasticsearchDao;
import fr.pilato.demo.legacysearch.dao.PersonDao;
import fr.pilato.demo.legacysearch.dao.SearchDao;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.helper.PersonGenerator;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class PersonService {
    final Logger logger = LoggerFactory.getLogger(PersonService.class);

    @Autowired PersonDao personDao;
    @Autowired SearchDao searchDao;
    @Autowired ElasticsearchDao elasticsearchDao;

    public Person get(String id) throws Exception {
        Person person = personDao.getByReference(id);
        if (logger.isDebugEnabled()) logger.debug("get({})={}", id, person);

        return person;
    }

    public Person save(Person person) {
        Person personDb = personDao.save(person);
        try {
            elasticsearchDao.save(person);
        } catch (Exception e) {
            logger.error("Houston, we have a problem!", e);
        }
        return personDb;
    }

    public boolean delete(String id) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("Person: {}", id);

        if (id == null) {
            return false;
        }

        Person person = get(id);
        if (person == null) {
            if (logger.isDebugEnabled()) logger.debug("Person with reference {} does not exist", id);
            return false;
        }
        personDao.delete(person);
        elasticsearchDao.delete(person.getReference());

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
                    .field("fulltext")
                    .field("name", 3.0f);
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
                        QueryBuilders.matchQuery("name.autocomplete", name)
                );
            }
            if (Strings.hasText(country)) {
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery("address.country.autocomplete", country)
                );
            }
            if (Strings.hasText(city)) {
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery("address.city.autocomplete", city)
                );
            }

            query = boolQueryBuilder;
        }

        SearchResponse response = elasticsearchDao.search(query, from, size);

        if (logger.isDebugEnabled()) logger.debug("advancedSearch({},{},{})={} persons", name, country, city, response.getHits().getTotalHits());

        return response.toString();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean init(Integer size) throws IOException {
        if (logger.isDebugEnabled()) logger.debug("Initializing database for {} persons", size);

        long start = System.currentTimeMillis();

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

        long took = System.currentTimeMillis() - start;

        if (logger.isDebugEnabled()) logger.debug("Database initialized with {} persons. Took: {} ms, around {} per second.",
                size, took, 1000 * size / took);

        return true;
    }
}
