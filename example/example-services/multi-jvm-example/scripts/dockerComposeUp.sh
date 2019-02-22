#!/usr/bin/env bash
set -e
docker-compose --no-ansi -f ./example/multi-jvm-example/multi-jvm-example-docker-services/docker-compose.yml -p example up -d --build
