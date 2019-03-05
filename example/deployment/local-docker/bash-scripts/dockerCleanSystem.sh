#!/usr/bin/env bash
set -e
. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
$DOCKERBASHSCRIPTS/dockerComposeDown.sh
docker system prune -a
