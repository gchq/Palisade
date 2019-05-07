#!/usr/bin/env bash
set -e
DIR=$(dirname "$0")
. "$DIR/../../bash-scripts/setScriptPath.sh"
docker-compose --no-ansi -f "$EXAMPLE/deployment/local-docker/example-docker-services/docker-compose.yml" -p example create --build
