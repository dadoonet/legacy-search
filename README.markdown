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
wget https://download.elasticsearch.org/elasticsearch/release/org/elasticsearch/distribution/tar/elasticsearch/2.0.0/elasticsearch-2.0.0.tar.gz
tar xzf elasticsearch-*.tar.gz
cd elasticsearch-*
```

### Optional (Kibana, Marvel and Sense)

Install [Kibana](https://www.elastic.co/downloads/kibana). On a Mac:

```
wget https://download.elastic.co/kibana/kibana/kibana-4.2.0-darwin-x64.tar.gz
tar xzf kibana-*.tar.gz
cd kibana-*
bin/kibana plugin --install elastic/sense
bin/kibana plugin --install elasticsearch/marvel/latest
```

Install Marvel plugin:

```
bin/plugin install license
bin/plugin install marvel-agent
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

You can open [Marvel](http://localhost:5601/app/marvel) and [Sense](http://localhost:5601/app/sense) if needed.


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
