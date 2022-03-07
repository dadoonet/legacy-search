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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.demo.legacysearch.dao.ElasticsearchDao;
import fr.pilato.demo.legacysearch.dao.PersonRepository;
import fr.pilato.demo.legacysearch.domain.Address;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.helper.PersonGenerator;
import fr.pilato.demo.legacysearch.helper.Strings;
import fr.pilato.demo.legacysearch.webapp.InitResult;
import fr.pilato.demo.legacysearch.webapp.PersonNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;

@Service
public class PersonService {
    private final Logger logger = LoggerFactory.getLogger(PersonService.class);

    @Value("${app.batch.size:100}")
    private int batchSize;

    private final PersonRepository personRepository;
    private final ObjectMapper mapper;
    private final Mapper dozerBeanMapper;
    private final ElasticsearchDao elasticsearchDao;

    public PersonService(PersonRepository personRepository, ObjectMapper mapper,
                         ElasticsearchDao elasticsearchDao,
                         Mapper dozerBeanMapper) {
        this.personRepository = personRepository;
        this.mapper = mapper;
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
        long start = System.nanoTime();

        Page<Person> page;

        if (Strings.isEmpty(q)) {
            page = personRepository.findAll(PageRequest.of(from / size, size));
        } else {
            page = personRepository.findLikeGoogle(q, PageRequest.of(from / size, size));
        }

        long total = page.getTotalElements();
        Collection<Person> personsFound = page.getContent();
        long took = (System.nanoTime() - start) / 1_000_000;

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

    public String advancedSearch(String name, String country, String city, Integer from, Integer size) throws IOException {
        Person person = new Person();
        if (name != null) {
            person.setName(name);
        }

        if (country != null || city != null) {
            Address address = new Address();
            if (country != null) {
                address.setCountry(country);
            }
            if (city != null) {
                address.setCity(city);
            }
            person.setAddress(address);
        }

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("name", contains().ignoreCase())
                .withMatcher("address.country", contains().ignoreCase())
                .withMatcher("address.city", contains().ignoreCase());

        Example<Person> example = Example.of(person, matcher);

        long start = System.nanoTime();

        Page<Person> page = personRepository.findAll(example, PageRequest.of(from / size, size));

        long total = page.getTotalElements();
        Collection<Person> personsFound = page.getContent();
        long took = (System.nanoTime() - start) / 1_000_000;

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

    public static class RestSearchTotal {
        private long value;

        public RestSearchTotal(long value) {
            this.value = value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }

    public static class RestSearchHits<T> {
        private RestSearchHit<T>[] hits;
        private RestSearchTotal total;

        public RestSearchTotal getTotal() {
            return total;
        }

        // Just for elasticsearch compatibility purpose
        @JsonIgnore
        public long getTotalHits() {
            return total.value;
        }

        public RestSearchHits<T> setTotal(long total) {
            this.total = new RestSearchTotal(total);
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
