#!/usr/bin/env bash
set -e
. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
docker-compose --no-ansi -f $RESTEXAMPLE/example-docker-services/docker-compose.yml -p example up -d --build
