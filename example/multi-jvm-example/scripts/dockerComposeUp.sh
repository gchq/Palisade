#!/usr/bin/env bash
set -e
docker-compose -f ./example/multi-jvm-example/multi-jvm-example-docker-services/docker-compose.yml -p palisadeexample up -d --build
