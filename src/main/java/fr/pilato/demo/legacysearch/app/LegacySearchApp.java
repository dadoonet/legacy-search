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

package fr.pilato.demo.legacysearch.app;


import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan("fr.pilato.demo.legacysearch")
@EnableJpaRepositories("fr.pilato.demo.legacysearch.dao")
@EntityScan("fr.pilato.demo.legacysearch.domain")
@SpringBootApplication
public class LegacySearchApp {
    private static final Logger logger = LoggerFactory.getLogger(LegacySearchApp.class);

    public static void main(String[] args) {
        logger.info("Starting LegacySearch demo application");
        SpringApplication.run(LegacySearchApp.class, args);
    }

    @Bean
    public DozerBeanMapper dozerBeanMapper() {
        logger.debug("creating dozen bean mapper");
        return new DozerBeanMapper();
    }
}
