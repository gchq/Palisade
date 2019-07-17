#!/usr/bin/env bash
set -e
CLEANETCD=$1
if [[ -n "$CLEANETCD" ]]; then
    DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
    . "$DIR/../../bash-scripts/setScriptPath.sh"
    if [[ "$CLEANETCD" = "TRUE" ]]; then
        docker-compose --no-ansi -f "$EXAMPLE/deployment/local-docker/example-docker-services/docker-compose-etcd.yml" -p example up -d --build
    else
        docker-compose --no-ansi -f "$EXAMPLE/deployment/local-docker/example-docker-services/docker-compose-no-etcd.yml" -p example up -d --build
    fi
else
   echo "argument error"
fi