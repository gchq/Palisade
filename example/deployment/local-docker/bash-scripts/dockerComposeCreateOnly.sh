#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"
docker-compose --no-ansi -f "$EXAMPLE/deployment/local-docker/example-docker-services/docker-compose.yml" -p example create --build
