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

package fr.pilato.demo.legacysearch;

import fr.pilato.demo.legacysearch.dao.HibernateUtils;
import fr.pilato.demo.legacysearch.domain.Person;
import fr.pilato.demo.legacysearch.helper.PersonGenerator;
import fr.pilato.demo.legacysearch.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class only for quick test purpose
 */
public class HibernateTester {
    final static Logger logger = LoggerFactory.getLogger(HibernateTester.class);

    public static void main(String[] args) throws Exception {

        try {
            PersonService service = new PersonService();
            Person person = service.save(PersonGenerator.personGenerator());

            String json = service.search("", null, null, 0, 10);
            logger.debug("json = {}", json);

            service.get(person.getId().toString());
            service.delete(person.getId().toString());
        } finally {
            HibernateUtils.getSession().close();
            HibernateUtils.getSessionFactory().close();
        }
    }
}
