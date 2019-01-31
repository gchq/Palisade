#!/usr/bin/env bash
set -e
docker-compose -f ./example/multi-jvm-example/multi-jvm-example-docker-services/docker-compose-part1.yml -p palisade_example down
docker-compose -f ./example/multi-jvm-example/multi-jvm-example-docker-services/docker-compose-part2.yml -p palisade_example down
