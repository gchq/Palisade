#!/usr/bin/env bash
set -e

docker-compose --no-ansi --verbose -f ./example/multi-jvm-example/multi-jvm-example-docker-services/docker-compose.yml -p palisade_example up -d --build