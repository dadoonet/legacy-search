#!/usr/bin/env bash

source .env

docker pull mysql:$MYSQL_VERSION
docker stop legacy-mysql
docker rm legacy-mysql
docker run --name legacy-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=person -d mysql:$MYSQL_VERSION
docker logs -f legacy-mysql

