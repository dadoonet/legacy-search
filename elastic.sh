#!/usr/bin/env bash

cd target
curl -fsSL https://elastic.co/start-local | ES_LOCAL_PASSWORD="changeme" sh
cd -
