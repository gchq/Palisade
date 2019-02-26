#!/usr/bin/env bash
set -e
. ./example/deployment/multi-use/bash-scripts/setScriptPath.sh
$DOCKERBASHSCRIPTS/dockerComposeDown.sh
docker system prune -a
