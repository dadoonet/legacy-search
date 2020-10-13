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

```
# Delete the index from Kibana Dev Console
DELETE person
```

When doing a live demo, you can also skip writing the create index part and manually create the index from Kibana Dev Console:

```
PUT person
{
  // Index settings here
}
```

You can get the index settings from this [script](https://gist.github.com/dadoonet/d6757d15fa0726a83bb619ecd81153f7)

Compile and restart the application

```
# Compile and launch again
mvn clean spring-boot:run

# Inject 10000 docs
curl http://127.0.0.1:8080/api/1/person/_init?size=10000
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).

You can use this [script](https://gist.github.com/dadoonet/d6757d15fa0726a83bb619ecd81153f7) to explain mapping and analyzers.

Next step
---------

Look at [branch 04-aggs](https://github.com/dadoonet/legacy-search/tree/04-aggs)
