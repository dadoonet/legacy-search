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
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.FieldDateMath;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.demo.legacysearch.domain.Person;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Component
public class ElasticsearchDao implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(ElasticsearchDao.class);

    private final ElasticsearchClient esClient;
    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
        @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
        @Override public X509Certificate[] getAcceptedIssuers() { return null; }
    }};
    private final JacksonJsonpMapper jacksonJsonpMapper;

    private final BulkIngester<Person> bulkIngester;

    public ElasticsearchDao(ObjectMapper mapper) throws IOException {
        String clusterUrl = "https://127.0.0.1:9200";
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));

        // Create the low-level client
        RestClient restClient = RestClient.builder(HttpHost.create(clusterUrl))
                .setHttpClientConfigCallback(hcb -> {
                            try {
                                SSLContext sslContext = SSLContext.getInstance("SSL");
                                sslContext.init(null, trustAllCerts, new SecureRandom());
                                hcb
                                    .setDefaultCredentialsProvider(credentialsProvider)
                                    .setSSLContext(sslContext)
                                    .setSSLHostnameVerifier((hostname, session) -> true);
                                return hcb;
                            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                                logger.warn("Failed to get SSL Context", e);
                                throw new RuntimeException(e);
                            }})
                .build();

        // Create the transport with a Jackson mapper
        jacksonJsonpMapper = new JacksonJsonpMapper(mapper);
        ElasticsearchTransport transport = new RestClientTransport(restClient, jacksonJsonpMapper);

        // And create the API client
        esClient = new ElasticsearchClient(transport);

        InfoResponse info = this.esClient.info();
        logger.info("Connected to {} running version {}", clusterUrl, info.version().number());

        // Create the person index
        try {
            esClient.indices().create(cir -> cir
                    .index("person")
                    .withJson(ElasticsearchDao.class.getResourceAsStream("/person.json"))
            );
            logger.info("New index person has been created");
        } catch (ElasticsearchException e) {
            if (e.status() != 400) {
                logger.warn("can not create index and mappings", e);
            } else {
                logger.debug("Index person was already existing. Skipping creating it again.");
            }
        }

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
                        .aggregations("by_country", ab -> ab.terms(tb -> tb.field("address.country.keyword"))
                          .aggregations("by_year", sab -> sab.dateHistogram(dhb -> dhb
                            .field("dateOfBirth")
                            .fixedInterval(d -> d.time("3653d"))
                            .extendedBounds(b -> b
                              .min(FieldDateMath.of(fdm -> fdm.expr("1940")))
                              .max(FieldDateMath.of(fdm -> fdm.expr("2009"))))
                              .format("8yyyy"))
                                .aggregations("avg_children", ssab -> ssab.avg(avg -> avg.field("children"))))
                        )
                        .aggregations("by_year", sab -> sab.dateHistogram(dhb -> dhb
                        .field("dateOfBirth")
                        .calendarInterval(CalendarInterval.Year)
                        .extendedBounds(b -> b
                          .min(FieldDateMath.of(fdm -> fdm.expr("1940")))
                          .max(FieldDateMath.of(fdm -> fdm.expr("2009"))))
                          .format("8yyyy")))
                , Person.class);

        return JsonpUtils.toJsonString(response, jacksonJsonpMapper);
    }

    @Override
    public void close() {
        bulkIngester.close();
    }
}
