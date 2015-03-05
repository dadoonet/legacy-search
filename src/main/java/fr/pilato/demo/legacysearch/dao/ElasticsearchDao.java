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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.demo.legacysearch.domain.Person;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ElasticsearchDao {
    final Logger logger = LoggerFactory.getLogger(ElasticsearchDao.class);

    final ObjectMapper mapper;
    final Client esClient;

    public ElasticsearchDao() {
        // Create a TransportClient to connect to our running node
        esClient = new TransportClient().addTransportAddress(
                new InetSocketTransportAddress("127.0.0.1", 9300)
        );
        // Create a Jackson Object Mapper instance
        mapper = new ObjectMapper();
    }

    public void save(Person person) throws JsonProcessingException {
        byte[] bytes = mapper.writeValueAsBytes(person);
        esClient.index(new IndexRequest("person", "person", person.getReference()).source(bytes)).actionGet();
    }

    public void delete(String reference) {
        esClient.delete(new DeleteRequest("person", "person", reference)).actionGet();
    }

    public SearchResponse search(QueryBuilder query, Integer from, Integer size) {
        logger.debug("elasticsearch query: {}", query.toString());
        SearchResponse response = esClient.prepareSearch("person")
                .setTypes("person")
                .setQuery(query)
                .setFrom(from)
                .setSize(size)
                .execute().actionGet();

        logger.debug("elasticsearch response: {} hits", response.getHits().totalHits());
        logger.trace("elasticsearch response: {} hits", response.toString());

        return response;
    }
}
