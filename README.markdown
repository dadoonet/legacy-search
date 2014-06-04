Add Search to Legacy Application
================================

Introduction
------------

This is a demo project to show how to add elasticsearch to a legacy SQL project.

This branch uses bulk mode to insert data.

Installation
------------

You need to have completed [branch 01-direct](https://github.com/dadoonet/legacy-search/tree/01-direct)

Run it!
-------

Compile and restart the application

```
# Compile and launch again
mvn clean package jetty:run

# Delete the index
DELETE person

# Inject 10000 docs
curl -XPOST "127.0.0.1:8080/api/1/person/_init?size=10000"
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).

Next step
---------

Look at [branch 03-mapping](https://github.com/dadoonet/legacy-search/tree/03-mapping)
