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

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.github.dozermapper.core.Mapper;
import fr.pilato.demo.legacysearch.dao.ElasticsearchDao;
import fr.pilato.demo.legacysearch.dao.PersonRepository;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.helper.PersonGenerator;
import fr.pilato.demo.legacysearch.helper.Strings;
import fr.pilato.demo.legacysearch.webapp.InitResult;
import fr.pilato.demo.legacysearch.webapp.PersonNotFoundException;
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

    @Value("${app.batch.size:10000}")
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

    private Iterable<Person> save(Collection<Person> persons) {
        Iterable<Person> personsDb = personRepository.saveAll(persons);
        try {
            elasticsearchDao.saveAll(personsDb);
        } catch (Exception e) {
            logger.error("Houston, we have a problem!", e);
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
        return save(Collections.singleton(person)).iterator().next();
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
        Query textQuery;
        // If the user does not provide any text to query, let's match all documents
        if (Strings.isEmpty(q)) {
            textQuery = Query.of(qb -> qb.matchAll(maq -> maq));
        } else {
            textQuery = Query.of(qb -> qb.multiMatch(
                    mm -> mm.query(q)
                            .fields("name^3",
                                    "name.ngram",
                                    "gender.ngram",
                                    "address.city.ngram",
                                    "address.country.ngram")));
        }

        return elasticsearchDao.search(textQuery, from, size);
    }

    public String advancedSearch(String name, String country, String city, Integer from, Integer size) throws IOException {
        Query query;

        // If the user does not provide any text to query, let's match all documents
        if (Strings.isEmpty(name) && Strings.isEmpty(country) && Strings.isEmpty(city)) {
            query = Query.of(qb -> qb.matchAll(maq -> maq));
        } else {
            query = Query.of(qb -> qb.bool(
                    bq -> {
                        if (Strings.hasText(name)) {
                            bq.must(mb -> mb.match(mq -> mq.field("name.ngram").query(name)));
                        }
                        if (Strings.hasText(country)) {
                            bq.must(mb -> mb.match(mq -> mq.field("address.country.ngram").query(country)));
                        }
                        if (Strings.hasText(city)) {
                            bq.must(mb -> mb.match(mq -> mq.field("address.city.ngram").query(city)));
                        }
                        return bq;
                    })
            );
        }

        return elasticsearchDao.search(query, from, size);
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
