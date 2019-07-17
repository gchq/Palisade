#!/bin/bash
set -e
CLEANETCD=$1
if [[ -n "$CLEANETCD" ]]; then
    DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
    . "$DIR/../../bash-scripts/setScriptPath.sh"
    "$DOCKERBASHSCRIPTS/dockerComposeDown.sh" "$CLEANETCD"
    docker system prune -a
else
   echo "argument error"
fi
