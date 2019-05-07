#!/usr/bin/env bash
set -e
DIR=$(dirname "$0")
. "$DIR/../../bash-scripts/setScriptPath.sh"
"$DOCKERBASHSCRIPTS/dockerComposeDown.sh"
docker system prune -a
