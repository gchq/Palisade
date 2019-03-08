#!/usr/bin/env bash
set -e
. ./example/deployment/bash-scripts/setScriptPath.sh
$DOCKERBASHSCRIPTS/dockerComposeDown.sh
docker system prune -a
