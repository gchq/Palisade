#!/usr/bin/env bash
# sets up the different paths for calling deployment scripts
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# Find example directory and normalise
export EXAMPLE="$(cd "$DIR/../.." >/dev/null && pwd -P)"
export DOCKERBASHSCRIPTS="$EXAMPLE/deployment/local-docker/bash-scripts"
export K8SBASHSCRIPTS="$EXAMPLE/deployment/local-k8s/bash-scripts"
export LOCALJVMBASHSCRIPTS="$EXAMPLE/deployment/local-jvm/bash-scripts"
export EXAMPLESERVICES="$EXAMPLE/example-services"
export GENERICSCRIPTS="$EXAMPLE/deployment/bash-scripts"
