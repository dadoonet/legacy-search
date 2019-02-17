Add Search to Legacy Application
================================

Introduction
------------

This is a demo project to show how to add elasticsearch to a legacy SQL project.

This branch connect our project to elasticsearch directly.

You need to have completed [branch 00-legacy](https://github.com/dadoonet/legacy-search/tree/00-legacy)

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

You can open [Kibana](http://localhost:5601/) after some seconds.


Manual Installation
------------

Download and unzip elasticsearch:

```
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.6.0.tar.gz
tar xzf elasticsearch-*.tar.gz
cd elasticsearch-*
```

### Optional (Kibana)

Install [Kibana](https://www.elastic.co/downloads/kibana). On a Mac:

```
wget https://artifacts.elastic.co/downloads/kibana/kibana-6.6.0-darwin-x86_64.tar.gz
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
