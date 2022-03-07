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
import fr.pilato.demo.legacysearch.helper.Strings;
import fr.pilato.demo.legacysearch.webapp.InitResult;
import fr.pilato.demo.legacysearch.webapp.PersonNotFoundException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PersonService {
    private final Logger logger = LoggerFactory.getLogger(PersonService.class);

    @Value("${app.batch.size:100}")
    private int batchSize;

    private final PersonRepository personRepository;
    private final Mapper dozerBeanMapper;
    private final ElasticsearchDao elasticsearchDao;

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
        return (save(Collections.singleton(person)).iterator().next());
    }

    private Iterable<Person> save(Collection<Person> persons) {
        Iterable<Person> personsDb = personRepository.saveAll(persons);
        try {
            elasticsearchDao.save(personsDb);
        } catch (IOException e) {
            logger.warn("Houston, we had a problem!", e);
        }
        logger.debug("Saved [{}] persons", persons.size());
        persons.clear();
        return personsDb;
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

    public void delete(Integer id) throws IOException {
        logger.debug("Person: {}", id);

        if (id != null) {
            personRepository.deleteById(id);
            elasticsearchDao.delete(id);
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
                        .field("name", 3.0f)
                        .field("name.ngram")
                        .field("gender.ngram")
                        .field("address.city.ngram")
                        .field("address.country.ngram");
        }

        if (Strings.hasText(f_country) || Strings.hasText(f_date)) {
            query = QueryBuilders.boolQuery().must(query);
            if (Strings.hasText(f_country)) {
                ((BoolQueryBuilder) query).filter(QueryBuilders.termQuery("address.country.keyword", f_country));
            }
            if (Strings.hasText(f_date)) {
                String endDate = "" + (Integer.parseInt(f_date) + 10);
                ((BoolQueryBuilder) query).filter(QueryBuilders.rangeQuery("dateOfBirth").gte(f_date).lt(endDate));
            }
        }

        SearchResponse response = elasticsearchDao.search(query, from, size);

        if (logger.isDebugEnabled()) logger.debug("search({},{},{})={} persons", q, f_country, f_date, response.getHits().getTotalHits());

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
                        QueryBuilders.matchQuery("name.ngram", name)
                );
            }
            if (Strings.hasText(country)) {
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery("address.country.ngram", country)
                );
            }
            if (Strings.hasText(city)) {
                boolQueryBuilder.must(
                        QueryBuilders.matchQuery("address.city.ngram", city)
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
        start = System.nanoTime();

        Collection<Person> persons = new ArrayList<>();

        Person joe = PersonGenerator.personGenerator();
        joe.setName("Joe Smith");
        joe.getAddress().setCountry("France");
        joe.getAddress().setCity("Paris");
        joe.getAddress().setCountrycode("FR");

        persons.add(joe);
        currentItem.incrementAndGet();

        Person franceGall = PersonGenerator.personGenerator();
        franceGall.setName("France Gall");
        franceGall.setGender("female");
        franceGall.getAddress().setCountry("Italy");
        franceGall.getAddress().setCity("Ischia");
        franceGall.getAddress().setCountrycode("IT");

        persons.add(franceGall);
        currentItem.incrementAndGet();

        // We generate numPersons persons and every batchSize, we send them to the DB
        for (int i = 2; i < size; i++) {
            Person person = PersonGenerator.personGenerator();
            persons.add(person);
            currentItem.incrementAndGet();
            if (i % batchSize == 0) {
                save(persons);
            }
        }

        // Save all remaining persons
        save(persons);

        long took = (System.nanoTime() - start) / 1_000_000;

        logger.debug("Database initialized with {} persons. Took: {} ms, around {} per second.",
                size, took, 1000L * size / took);

        return new InitResult(took, 1000L * size / took, size);
    }

    public InitResult getInitCurrentAchievement() {
        int current = currentItem.get();
        long took = (System.nanoTime() - start) / 1_000_000;
        return new InitResult(took, 1000L * current / took, current);
    }
}
