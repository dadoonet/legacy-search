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
import fr.pilato.elasticsearch.tools.ElasticsearchBeyonder;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import javax.inject.Inject;
import java.net.InetSocketAddress;

@Component
public class ElasticsearchDao {
    final Logger logger = LoggerFactory.getLogger(ElasticsearchDao.class);

    final private ObjectMapper mapper;
    final private Client esClient;
    final private BulkProcessor bulkProcessor;

    @Inject
    public ElasticsearchDao(ObjectMapper mapper) {
        this.esClient = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("127.0.0.1", 9300)));
        // Automagically create index and mapping
        try {
            ElasticsearchBeyonder.start(esClient);
        } catch (Exception e) {
            logger.warn("can not create index and mappings", e);
        }
        this.mapper = mapper;
        this.bulkProcessor = BulkProcessor.builder(esClient, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                logger.debug("going to execute bulk of {} requests", request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                logger.debug("bulk executed {} failures", response.hasFailures() ? "with" : "without");
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                logger.warn("error while executing bulk", failure);
            }
        })
                .setBulkActions(10000)
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .build();
    }

    public void save(Person person) throws JsonProcessingException {
        byte[] bytes = mapper.writeValueAsBytes(person);
        bulkProcessor.add(new IndexRequest("person", "person", person.idAsString()).source(bytes, XContentType.JSON));
    }

    public void delete(String id) {
        bulkProcessor.add(new DeleteRequest("person", "person", id));
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
