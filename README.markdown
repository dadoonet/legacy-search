Add Search to Legacy Application
================================

Introduction
------------

This is a demo project to show how to add elasticsearch to a legacy SQL project.

In this branch, you will find the current legacy version of the project.


Installation
------------

You need to have:

* Maven
* JDK7 or higher
* Postgresql up and running

Modify [src/main/resources/jdbc.properties](src/main/resources/jdbc.properties) file to reflect
your own postgresql settings:

```
jdbc.driverClassName=org.postgresql.Driver
jdbc.url=jdbc:postgresql://localhost:5432/dpilato
jdbc.username=dpilato
jdbc.password=
```

Start the server using jetty

```sh
mvn clean install
mvn jetty:run
```

Play!
-----

### Some CRUD operations

```sh
# Create one person
curl -XPUT http://127.0.0.1:8080/api/1/person/1 -d '{"name":"David Pilato"}'

# Read that person
curl http://127.0.0.1:8080/api/1/person/1

# Update (will merge values)
curl -XPUT http://127.0.0.1:8080/api/1/person/1 -d '{"children":3}'
# or update full document
curl -XPUT http://127.0.0.1:8080/api/1/person/1 -d '{"name":"David Pilato", "children":3}'

# Check
curl http://127.0.0.1:8080/api/1/person/1

# Update (full document)
curl -XPUT http://127.0.0.1:8080/api/1/person/1 -d '{"name":"David Pilato", "children":3}'

# Delete
curl -XDELETE http://127.0.0.1:8080/api/1/person/1
```

### Database Initialisation

```sh
# Initialize the database with 10 persons
curl -XPOST "127.0.0.1:8080/api/1/person/_init?size=10000"
```

## Search

```sh
# Search for something (`a la google`)
curl -XPOST "http://127.0.0.1:8080/api/1/person/_search?q=John&from=0&size=10"
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).

You can also look at [advanced search](http://127.0.0.1:8080/#/advanced).


Next step
---------

Look at [branch 01-direct](https://github.com/dadoonet/legacy-search/tree/01-direct)
