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
package fr.pilato.demo.legacysearch.webapp;

import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class PersonController {
    private final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/api/1/person/{id}")
    public Person get(@PathVariable Integer id) {
        return personService.get(id);
    }

    /**
     * Create or update an entity
     */
    @PutMapping("/api/1/person/{id}")
    public Person upsert(@PathVariable Integer id, @RequestBody Person person) {
        logger.debug("upsert({}, {})", id, person);
        Person upsert = personService.upsert(id, person);
        logger.debug("created/updated {}: {}", id, upsert);
        return upsert;
    }

    @DeleteMapping("/api/1/person/{id}")
    public void delete(@PathVariable Integer id) throws IOException {
        personService.delete(id);
    }

    @GetMapping("/api/1/person/_search")
    public String search(@RequestParam(required = false) String q, @RequestParam(required = false) String f_country,
                         @RequestParam(required = false) String f_date, @RequestParam(required = false, defaultValue = "0") Integer from,
                         @RequestParam(required = false, defaultValue = "10") Integer size) throws IOException {
        return personService.search(q, f_country, f_date, from, size);
    }

    @GetMapping("/api/1/person/_advanced_search")
    public String advancedSearch(@RequestParam(required = false) String name, @RequestParam(required = false) String country,
                                 @RequestParam(required = false) String city,
                                 @RequestParam(required = false, defaultValue = "0") Integer from,
                                 @RequestParam(required = false, defaultValue = "10") Integer size) throws IOException {
        return personService.advancedSearch(name, country, city, from, size);
    }

    @GetMapping("/api/1/person/_init")
    public InitResult init(@RequestParam(required = false, defaultValue = "1000") Integer size) throws IOException {
        return personService.init(size);
    }

    @GetMapping("/api/1/person/_init_status")
    public InitResult initStatus() {
        return personService.getInitCurrentAchievement();
    }
}
