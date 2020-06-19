Add Search to Legacy Application
================================

Introduction
------------

This is a demo project to show how to add elasticsearch to a legacy SQL project.

This branch connect our project to elasticsearch directly.

You need to have completed [branch 00-legacy](https://github.com/dadoonet/legacy-search/tree/00-legacy)

Running on cloud
----------------

If you want to run the demo using https://cloud.elastic.co, create a new
instance (2gb of RAM should be enough) and change in `ElasticsearchDao`
class the `CLOUD_URL` of the cluster (copy it from the cloud console)
and the `CLOUD_PASSWORD`. Alternatively, you can also create a dedicated
user for this demo and use it.

```java
final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "CLOUD_PASSWORD"));
this.esClient = new RestHighLevelClient(RestClient.builder(HttpHost.create("CLOUD_URL"))
  .setHttpClientConfigCallback(hcb -> hcb.setDefaultCredentialsProvider(credentialsProvider)));
```

Docker Compose Installation
------------

You must have Docker installed obviously and 4gb memory assigned to it.
Or you can edit `docker-compose.yml` file and replace the following line depending on what memory you assigned:

```
- "ES_JAVA_OPTS=-Xms2g -Xmx2g"
```

Best practice: no more than the half of available RAM.

Then run:

```sh
docker-compose up
```

You can open [Kibana](http://localhost:5601/) after some seconds and
connect using `elastic` user with `changeme` as the password.


Manual Installation
------------

Install [elasticsearch](https://www.elastic.co/downloads/elasticsearch). On a Mac:

```
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.8.0-darwin-x86_64.tar.gz
tar xzf elasticsearch-*.tar.gz
cd elasticsearch-*
```

### Optional (Kibana)

Install [Kibana](https://www.elastic.co/downloads/kibana). On a Mac:

```
wget https://artifacts.elastic.co/downloads/kibana/kibana-7.8.0-darwin-x86_64.tar.gz
tar xzf kibana-*.tar.gz
cd kibana-*
```

### Launch elasticsearch

Launch elasticsearch:

```
bin/elasticsearch
```

### Launch Kibana (optional)

```
bin/kibana
```

You can open [Console](http://localhost:5601/app/console) if needed.


Run it!
-------

Compile and restart the application

```
mvn clean spring-boot:run
curl http://127.0.0.1:8080/api/1/person/_init?size=10000
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).

Next step
---------

Look at [branch 02-bulk](https://github.com/dadoonet/legacy-search/tree/02-bulk)
