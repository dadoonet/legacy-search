Add Search to Legacy Application
================================

Introduction
------------

This is a demo project to show how to add elasticsearch to a legacy SQL project.

This branch add compute dashboard using aggregations.

Installation
------------

You need to have completed [branch 04-aggs](https://github.com/dadoonet/legacy-search/tree/04-aggs)

Run it!
-------

Compile and restart the application

```
# Compile and launch again
mvn clean spring-boot:run

# We don't need to reindex data as they are already in elasticsearch.
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).

Next step
---------

Look at [branch 06-fuzziness](https://github.com/dadoonet/legacy-search/tree/06-fuzziness)
