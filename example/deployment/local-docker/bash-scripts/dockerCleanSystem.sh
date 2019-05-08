#!/bin/bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"
"$DOCKERBASHSCRIPTS/dockerComposeDown.sh"
docker system prune -a
