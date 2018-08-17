#!/usr/bin/env bash
set -e
./example/multi-jvm-example/scripts/dockerComposeDown.sh
docker system prune -a
