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
wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.7.1.tar.gz
tar xzf elasticsearch-*.tar.gz
cd elasticsearch-*
```

Install Marvel plugin (optionnal):

```
bin/plugin -i elasticsearch/marvel/latest
```

Launch elasticsearch:

```
bin/elasticsearch
```

You can open [Marvel](http://localhost:9200/_plugin/marvel/) if needed.

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
