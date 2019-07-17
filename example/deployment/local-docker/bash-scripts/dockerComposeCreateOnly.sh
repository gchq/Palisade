#!/bin/bash
CLEANETCD=$1
if [[ -n "$CLEANETCD" ]]; then
    DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
    . "$DIR/../../bash-scripts/setScriptPath.sh"
    if [[ "$CLEANETCD" = "TRUE" ]]; then
        docker-compose --no-ansi -f "$EXAMPLE/deployment/local-docker/example-docker-services/docker-compose-etcd.yml" -p example create --build
    else
        docker-compose --no-ansi -f "$EXAMPLE/deployment/local-docker/example-docker-services/docker-compose-no-etcd.yml" -p example create --build
    fi
else
   echo "argument error"
fi
