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
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchDao {
    private final Logger logger = LoggerFactory.getLogger(ElasticsearchDao.class);

    private final ObjectMapper mapper;
    private final RestHighLevelClient esClient;

    public ElasticsearchDao(ObjectMapper mapper) throws IOException {
        String clusterUrl = "http://127.0.0.1:9200";
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));
        this.esClient = new RestHighLevelClient(RestClient.builder(HttpHost.create(clusterUrl))
                .setHttpClientConfigCallback(hcb -> hcb.setDefaultCredentialsProvider(credentialsProvider)));

        MainResponse info = this.esClient.info(RequestOptions.DEFAULT);
        logger.info("Connected to {} running version {}", clusterUrl, info.getVersion().getNumber());

        this.mapper = mapper;
    }

    public void save(Iterable<Person> persons) throws IOException {
        BulkRequest bulkRequest = new BulkRequest("person");
        persons.forEach(p -> {
            try {
                bulkRequest.add(new IndexRequest().source(mapper.writeValueAsBytes(p), XContentType.JSON));
            } catch (JsonProcessingException e) {
                logger.warn("Can not serialize to JSON", e);
            }
        });
        BulkResponse bulkResponse = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            logger.warn("We got failures... {}", bulkResponse.buildFailureMessage());
        }
    }

    public void delete(Integer id) throws IOException {
        esClient.delete(new DeleteRequest("person", "" + id), RequestOptions.DEFAULT);
    }
}
