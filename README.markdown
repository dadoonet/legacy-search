Add Search to Legacy Application
================================

Introduction
------------

This is a demo project to show how to add elasticsearch to a legacy SQL project.

This branch connect our project to elasticsearch directly.

Installation
------------

You need to have completed [branch 00-legacy](https://github.com/dadoonet/legacy-search/tree/00-legacy)

Download and unzip elasticsearch:

```
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.4.2.tar.gz
tar xzf elasticsearch-*.tar.gz
cd elasticsearch-*
```

### Optional (Kibana)

Install [Kibana](https://www.elastic.co/downloads/kibana). On a Mac:

```
wget https://artifacts.elastic.co/downloads/kibana/kibana-5.4.2-darwin-x86_64.tar.gz
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
mvn clean package jetty:run
curl http://127.0.0.1:8080/api/1/person/_init?size=10000
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).

Next step
---------

Look at [branch 02-bulk](https://github.com/dadoonet/legacy-search/tree/02-bulk)
