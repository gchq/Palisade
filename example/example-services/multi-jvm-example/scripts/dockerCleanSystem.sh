#!/usr/bin/env bash
set -e
./example/example-services/multi-jvm-example/scripts/dockerComposeDown.sh
docker system prune -a
