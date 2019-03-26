#!/usr/bin/env bash
# sets up the different paths for calling deployment scripts
PWD=$(pwd)
export DOCKERBASHSCRIPTS=$PWD/example/deployment/local-docker/bash-scripts
export LOCALJVMBASHSCRIPTS=$PWD/example/deployment/local-jvm/bash-scripts
export EXAMPLESERVICES=$PWD/example/example-services
export EXAMPLE=$PWD/example
