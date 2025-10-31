#!/usr/bin/env bash

docker pull mysql:latest
docker stop legacy-mysql
docker rm legacy-mysql
docker run --name legacy-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=person -d mysql:latest
docker logs -f legacy-mysql

# Stop the database when done
docker stop legacy-mysql
docker rm legacy-mysql
