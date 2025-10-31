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

package fr.pilato.demo.legacysearch.dao;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.demo.legacysearch.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class ElasticsearchDao implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(ElasticsearchDao.class);

    private final ElasticsearchClient esClient;
    private final JacksonJsonpMapper jacksonJsonpMapper;

    private final BulkIngester<Person> bulkIngester;

    public ElasticsearchDao(ObjectMapper mapper) throws IOException {
        String clusterUrl = "http://127.0.0.1:9200";
        jacksonJsonpMapper = new JacksonJsonpMapper(mapper);
        esClient = ElasticsearchClient.of(b -> b
                .host(clusterUrl)
                .usernameAndPassword("elastic", "changeme")
                .jsonMapper(jacksonJsonpMapper)
                // .apiKey("OR-BETTER-PASTE-THE-APIKEY-HERE")
        );

        InfoResponse info = this.esClient.info();
        logger.info("Connected to {} running version {}", clusterUrl, info.version().number());

        // Use the BulkIngester helper
        bulkIngester = BulkIngester.of(bi -> bi
                .client(esClient)
                .maxOperations(10000)
                .flushInterval(5, TimeUnit.SECONDS));
    }

    public void saveAll(Iterable<Person> persons) {
        persons.forEach(person -> bulkIngester.add(o -> o.index(i -> i
                .index("person")
                .id(person.idAsString())
                .document(person)
        )));
    }

    public void delete(Integer id) {
        bulkIngester.add(o -> o.delete(dr -> dr
                .index("person")
                .id(String.valueOf(id))
        ));
    }

    public String search(Query query, Integer from, Integer size) throws IOException {
        SearchResponse<Person> response = esClient.search(sr -> sr
                        .index("person")
                        .query(query)
                        .from(from)
                        .size(size)
                        .trackTotalHits(tth -> tth.enabled(true))
                , Person.class);

        return JsonpUtils.toJsonString(response, jacksonJsonpMapper);
    }

    @Override
    public void close() {
        bulkIngester.close();
    }
}
