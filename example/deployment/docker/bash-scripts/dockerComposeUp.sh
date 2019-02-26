#!/usr/bin/env bash
set -e
. ./example/deployment/multi-use/bash-scripts/setScriptPath.sh
docker-compose --no-ansi -f $MULTIJVMEXAMPLE/multi-jvm-example-docker-services/docker-compose.yml -p example up -d --build
