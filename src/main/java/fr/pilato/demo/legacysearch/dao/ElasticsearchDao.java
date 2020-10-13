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
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchDao {
    private final Logger logger = LoggerFactory.getLogger(ElasticsearchDao.class);

    private final ObjectMapper mapper;
    private final RestHighLevelClient esClient;
    private final BulkProcessor bulkProcessor;

    public ElasticsearchDao(ObjectMapper mapper) throws IOException {
        String clusterUrl = "http://127.0.0.1:9200";
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));
        this.esClient = new RestHighLevelClient(RestClient.builder(HttpHost.create(clusterUrl))
                .setHttpClientConfigCallback(hcb -> hcb.setDefaultCredentialsProvider(credentialsProvider)));

        MainResponse info = this.esClient.info(RequestOptions.DEFAULT);
        logger.info("Connected to {} running version {}", clusterUrl, info.getVersion().getNumber());

        try {
            this.esClient.indices().create(new CreateIndexRequest("person").source("{\n" +
                            "    \"settings\": {\n" +
                            "        \"analysis\": {\n" +
                            "            \"analyzer\": {\n" +
                            "                \"ngram\": {\n" +
                            "                    \"tokenizer\": \"ngram_tokenizer\",\n" +
                            "                    \"filter\": [\n" +
                            "                        \"lowercase\"\n" +
                            "                    ]\n" +
                            "                }\n" +
                            "            },\n" +
                            "            \"tokenizer\": {\n" +
                            "                \"ngram_tokenizer\": {\n" +
                            "                    \"type\": \"edge_ngram\",\n" +
                            "                    \"min_gram\": \"1\",\n" +
                            "                    \"max_gram\": \"10\",\n" +
                            "                    \"token_chars\": [\n" +
                            "                        \"letter\",\n" +
                            "                        \"digit\"\n" +
                            "                    ]\n" +
                            "                }\n" +
                            "            }\n" +
                            "        }\n" +
                            "    },\n" +
                            "    \"mappings\": {\n" +
                            "        \"properties\": {\n" +
                            "            \"address\": {\n" +
                            "                \"properties\": {\n" +
                            "                    \"city\": {\n" +
                            "                        \"type\": \"text\",\n" +
                            "                        \"fields\": {\n" +
                            "                            \"ngram\": {\n" +
                            "                                \"type\": \"text\",\n" +
                            "                                \"analyzer\": \"ngram\",\n" +
                            "                                \"search_analyzer\": \"simple\"\n" +
                            "                            },\n" +
                            "                            \"keyword\": {\n" +
                            "                                \"type\": \"keyword\"\n" +
                            "                            }\n" +
                            "                        }\n" +
                            "                    },\n" +
                            "                    \"country\": {\n" +
                            "                        \"type\": \"text\",\n" +
                            "                        \"fields\": {\n" +
                            "                            \"ngram\": {\n" +
                            "                                \"type\": \"text\",\n" +
                            "                                \"analyzer\": \"ngram\",\n" +
                            "                                \"search_analyzer\": \"simple\"\n" +
                            "                            },\n" +
                            "                            \"keyword\": {\n" +
                            "                                \"type\": \"keyword\"\n" +
                            "                            }\n" +
                            "                        }\n" +
                            "                    },\n" +
                            "                    \"countrycode\": {\n" +
                            "                        \"type\": \"keyword\"\n" +
                            "                    },\n" +
                            "                    \"location\": {\n" +
                            "                        \"type\": \"geo_point\"\n" +
                            "                    },\n" +
                            "                    \"zipcode\": {\n" +
                            "                        \"type\": \"keyword\"\n" +
                            "                    }\n" +
                            "                }\n" +
                            "            },\n" +
                            "            \"children\": {\n" +
                            "                \"type\": \"long\"\n" +
                            "            },\n" +
                            "            \"dateOfBirth\": {\n" +
                            "                \"type\": \"date\",\n" +
                            "                \"format\": \"yyyy-MM-dd||yyyy\"\n" +
                            "            },\n" +
                            "            \"gender\": {\n" +
                            "                \"type\": \"text\",\n" +
                            "                \"fields\": {\n" +
                            "                    \"ngram\": {\n" +
                            "                        \"type\": \"text\",\n" +
                            "                        \"analyzer\": \"ngram\",\n" +
                            "                        \"search_analyzer\": \"simple\"\n" +
                            "                    },\n" +
                            "                    \"keyword\": {\n" +
                            "                        \"type\": \"keyword\"\n" +
                            "                    }\n" +
                            "                }\n" +
                            "            },\n" +
                            "            \"marketing\": {\n" +
                            "                \"properties\": {\n" +
                            "                    \"cars\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    },\n" +
                            "                    \"electronic\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    },\n" +
                            "                    \"fashion\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    },\n" +
                            "                    \"food\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    },\n" +
                            "                    \"garden\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    },\n" +
                            "                    \"hifi\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    },\n" +
                            "                    \"music\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    },\n" +
                            "                    \"shoes\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    },\n" +
                            "                    \"toys\": {\n" +
                            "                        \"type\": \"long\"\n" +
                            "                    }\n" +
                            "                }\n" +
                            "            },\n" +
                            "            \"name\": {\n" +
                            "                \"type\": \"text\",\n" +
                            "                \"fields\": {\n" +
                            "                    \"ngram\": {\n" +
                            "                        \"type\": \"text\",\n" +
                            "                        \"analyzer\": \"ngram\",\n" +
                            "                        \"search_analyzer\": \"simple\"\n" +
                            "                    }\n" +
                            "                }\n" +
                            "            },\n" +
                            "            \"reference\": {\n" +
                            "                \"type\": \"text\"\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n" +
                            "}\n", XContentType.JSON)
                    , RequestOptions.DEFAULT);
            logger.info("New index person has been created");
        } catch (ElasticsearchStatusException e) {
            if (e.status().getStatus() != 400) {
                logger.warn("can not create index and mappings", e);
            } else {
                logger.debug("Index person was already existing. Skipping creating it again.");
            }
        }
        this.mapper = mapper;
        this.bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) -> esClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                new BulkProcessor.Listener() {
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
        bulkProcessor.add(new IndexRequest("person").id(person.idAsString()).source(bytes, XContentType.JSON));
    }

    public void delete(String id) {
        bulkProcessor.add(new DeleteRequest("person", id));
    }

    public SearchResponse search(QueryBuilder query, Integer from, Integer size) throws IOException {
        logger.debug("elasticsearch query: {}", query.toString());
        SearchResponse response = esClient.search(new SearchRequest("person")
                .source(new SearchSourceBuilder()
                        .query(query)
                        .from(from)
                        .size(size)
                        .trackTotalHits(true)
                        .sort(SortBuilders.scoreSort())
                        .sort(SortBuilders.fieldSort("dateOfBirth"))
                ), RequestOptions.DEFAULT);

        logger.debug("elasticsearch response: {} hits", response.getHits().getTotalHits());
        logger.trace("elasticsearch response: {} hits", response.toString());

        return response;
    }
}
