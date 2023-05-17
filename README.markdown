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
* JDK8 or higher
* Docker

Run MySQL database using docker with:

```shell
./mysql.sh
```

Build the application:

```sh
mvn clean install
```

Then run it with:

```
java -jar target/legacy-search-8.0-SNAPSHOT.jar
```

Or directly run from Maven:

```sh
mvn clean spring-boot:run
```

Note that while developing, you would probably prefer running `LegacySearchApp#main()`
from your IDE to get hot reload of the application.

Play!
-----

### Some CRUD operations

```sh
# Create one person
curl -XPUT http://127.0.0.1:8080/api/1/person/1 -H "Content-Type: application/json" -d '{"name":"David Pilato"}'

# Read that person
curl http://127.0.0.1:8080/api/1/person/1

# Update full document
curl -XPUT http://127.0.0.1:8080/api/1/person/1 -H "Content-Type: application/json" -d '{"name":"David Pilato", "children":3}'

# Check
curl http://127.0.0.1:8080/api/1/person/1

# Delete
curl -XDELETE http://127.0.0.1:8080/api/1/person/1

# Check (you should get a 404 error)
curl http://127.0.0.1:8080/api/1/person/1
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
curl "http://127.0.0.1:8080/api/1/person/_search?q=Joe"
```

You can then access the application using your browser: [http://127.0.0.1:8080/](http://127.0.0.1:8080/).
You can also look at [advanced search](http://127.0.0.1:8080/#/advanced).

Next step
---------

Look at [branch 01-direct](https://github.com/dadoonet/legacy-search/tree/01-direct)
