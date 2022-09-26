Add Search to Legacy Application
================================

Introduction
------------

This is a demo project to show how to add elasticsearch to a legacy SQL project.

This branch connects our project to elasticsearch directly using Bulk.

You need to have completed [branch 00-legacy](https://github.com/dadoonet/legacy-search/tree/00-legacy)

Running on cloud
----------------

If you want to run the demo using https://cloud.elastic.co, create a new
instance (2gb of RAM should be enough) and change in `ElasticsearchDao`
class the `CLOUD_URL` of the cluster (copy it from the cloud console)
and the `CLOUD_PASSWORD`. Alternatively, you can also create a dedicated
user for this demo and use it.

```java
String clusterUrl = "CLOUD_URL";
final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "CLOUD_PASSWORD"));
```

Docker Compose Installation
------------

Run:

```sh
docker-compose up
```

You can open [Kibana](http://localhost:5601/) after some seconds and
connect using `elastic` user with `changeme` as the password.

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

Look at [branch 02-search](https://github.com/dadoonet/legacy-search/tree/02-search)
