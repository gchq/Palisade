#!/usr/bin/env bash
set -e
docker-compose --no-ansi -f ./example/example-services/multi-jvm-example/multi-jvm-example-docker-services/docker-compose.yml -p example down