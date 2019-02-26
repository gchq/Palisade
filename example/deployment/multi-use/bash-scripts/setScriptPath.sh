#!/usr/bin/env bash
# sets up the different paths for calling deployment scripts
PWD=$(pwd)
export DOCKERBASHSCRIPTS=$PWD/example/deployment/docker/bash-scripts
export LOCALJVMBASHSCRIPTS=$PWD/example/deployment/local-jvm/bash-scripts
export MULTIUSEBASHSCRIPTS=$PWD/example/deployment/multi-use/bash-scripts
export MULTIJVMEXAMPLE=$PWD/example/example-services/multi-jvm-example
export EXAMPLESERVICES=$PWD/example/example-services/