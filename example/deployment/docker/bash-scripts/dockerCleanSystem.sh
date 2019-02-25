#!/usr/bin/env bash
set -e
./example/deployment/multi-use/bash-scripts/setScriptPath.sh
echo $DOCKERBASHSCRIPTS
$DOCKERBASHSCRIPTS/dockerComposeDown.sh
docker system prune -a
