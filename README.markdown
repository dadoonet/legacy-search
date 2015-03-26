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

Modify [src/main/resources/hibernate.cfg.xml](src/main/resources/hibernate.cfg.xml) file to reflect
your own postgresql settings:

```xml
<!-- Database connection settings -->
<property name="hibernate.connection.url">jdbc:postgresql://localhost:5432/dpilato</property>
<property name="hibernate.connection.username">dpilato</property>
<property name="hibernate.connection.password"></property>
```

Start the server using jetty

```sh
mvn clean install
mvn jetty:run
```

Note that while developing, you would probably prefer running `LegacySearchApp#main()`
which will scan your modification and will perform hot reload.

Play!
-----

### Some CRUD operations

```sh
# Create one person
curl -XPUT http://127.0.0.1:8080/api/1/person/1 -d '{"name":"David Pilato"}'

# Read that person
curl http://127.0.0.1:8080/api/1/person/_byid/1

# Update full document
curl -XPUT http://127.0.0.1:8080/api/1/person/1 -d '{"name":"David Pilato", "children":3}'

# Check
curl http://127.0.0.1:8080/api/1/person/1

# Delete
curl -XDELETE http://127.0.0.1:8080/api/1/person/1
```

### Database Initialisation

```sh
# Initialize the database with 1 000 (default) or 10 000 persons
curl http://127.0.0.1:8080/api/1/person/_init
curl http://127.0.0.1:8080/api/1/person/_init?size=10000
```

## Search

```sh
# Search for something (`a la google`)
curl "http://127.0.0.1:8080/api/1/person/_search?q=Joe&from=0&size=10"
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).
You can also look at [advanced search](http://127.0.0.1:8080/#/advanced).

RestX provides as well its own interface: [http://127.0.0.1:8080/api/@/ui/](http://127.0.0.1:8080/api/@/ui/).
Default login / password are: `admin` / `juma`.

Next step
---------

Look at [branch 01-direct](https://github.com/dadoonet/legacy-search/tree/01-direct)
