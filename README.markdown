Add Search to Legacy Application
================================

Introduction
------------

This is a demo project to show how to add elasticsearch to a legacy SQL project.

This branch modifies default mapping for elasticsearch documents
and will provide search by prefix feature.

Installation
------------

You need to have completed [branch 02-bulk](https://github.com/dadoonet/legacy-search/tree/02-bulk)

Run it!
-------

Compile and restart the application

```
# Delete the index
DELETE person

mvn clean package jetty:run

# Inject 10000 docs
curl http://127.0.0.1:8080/api/1/person/_init?size=10000
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).

You can use this [script](https://gist.github.com/dadoonet/d6757d15fa0726a83bb619ecd81153f7) to explain informations about mapping and analyzers.

Next step
---------

Look at [branch 04-aggs](https://github.com/dadoonet/legacy-search/tree/04-aggs)
