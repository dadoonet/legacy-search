/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.pilato.demo.legacysearch.service;

import com.github.dozermapper.core.Mapper;
import fr.pilato.demo.legacysearch.dao.ElasticsearchDao;
import fr.pilato.demo.legacysearch.dao.PersonRepository;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.helper.PersonGenerator;
import fr.pilato.demo.legacysearch.webapp.InitResult;
import fr.pilato.demo.legacysearch.webapp.PersonNotFoundException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PersonService {
    private final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final Mapper dozerBeanMapper;
    private final ElasticsearchDao elasticsearchDao;

    @Autowired
    public PersonService(PersonRepository personRepository,
                         ElasticsearchDao elasticsearchDao,
                         Mapper dozerBeanMapper) {
        this.personRepository = personRepository;
        this.dozerBeanMapper = dozerBeanMapper;
        this.elasticsearchDao = elasticsearchDao;
    }

    public Person get(Integer id) {
        Person person = personRepository.findById(id).orElseThrow(PersonNotFoundException::new);
        logger.debug("get({})={}", id, person);
        return person;
    }

    public Person save(Person person) {
        Person personDb = personRepository.save(person);
        try {
            elasticsearchDao.save(personDb);
        } catch (Exception e) {
            logger.error("Houston, we have a problem!", e);
        }

        logger.debug("Saved person [{}]", personDb.getId());
        return personDb;
    }

    public Person upsert(Integer id, Person person) {
        // We try to find an existing document
        try {
            Person personDb = get(id);
            dozerBeanMapper.map(person, personDb);
            person = personDb;
            person.setId(id);
        } catch (PersonNotFoundException ignored) { }
        person = save(person);

        return person;
    }

    public void delete(Integer id) {
        logger.debug("Person: {}", id);

        if (id != null) {
            personRepository.deleteById(id);
            elasticsearchDao.delete("" + id);
        }

        logger.debug("Person deleted: {}", id);
    }

    public String search(String q, String f_country, String f_date, Integer from, Integer size) throws IOException {
        QueryBuilder query;
        // If the user does not provide any text to query, let's match all documents
        if (Strings.isEmpty(q)) {
            query = QueryBuilders.matchAllQuery();
        } else {
            query = QueryBuilders
                    .multiMatchQuery(q)
                        .field("fulltext")
                        .field("name", 3.0f);
        }

        SearchResponse response = elasticsearchDao.search(query, from, size);

        if (logger.isDebugEnabled()) logger.debug("search({})={} persons", q, response.getHits().getTotalHits());

        return response.toString();
    }

    public String advancedSearch(String name, String country, String city, Integer from, Integer size) throws IOException {
        QueryBuilder query;

        // If the user does not provide any text to query, let's match all documents
        if (Strings.isEmpty(name) && Strings.isEmpty(country) && Strings.isEmpty(city)) {
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

        logger.debug("advancedSearch({},{},{})={} persons", name, country, city, response.getHits().getTotalHits());

        return response.toString();
    }

    private AtomicInteger currentItem = new AtomicInteger();
    private long start = 0;

    public InitResult init(Integer size) throws IOException {
        currentItem.set(0);

        logger.debug("Initializing database for {} persons", size);
        start = System.currentTimeMillis();

        Person joe = PersonGenerator.personGenerator();
        joe.setName("Joe Smith");
        joe.getAddress().setCountry("France");
        joe.getAddress().setCity("Paris");
        joe.getAddress().setCountrycode("FR");

        save(joe);
        currentItem.incrementAndGet();

        Person franceGall = PersonGenerator.personGenerator();
        franceGall.setName("France Gall");
        franceGall.setGender("female");
        franceGall.getAddress().setCountry("Italy");
        franceGall.getAddress().setCity("Ischia");
        franceGall.getAddress().setCountrycode("IT");

        save(franceGall);
        currentItem.incrementAndGet();

        // We generate numPersons persons
        for (int i = 2; i < size; i++) {
            Person person = PersonGenerator.personGenerator();
            save(person);
            currentItem.incrementAndGet();
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
}
