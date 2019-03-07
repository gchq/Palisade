#!/usr/bin/env bash
set -e
. ./example/deployment/bash-scripts/setScriptPath.sh
docker-compose --no-ansi -f $PWD/example/deployment/local-docker/example-docker-services/docker-compose.yml -p example up -d --build
