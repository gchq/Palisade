#!/usr/bin/env bash
# sets up the different paths for calling deployment scripts
PWD=$(pwd)
export DOCKERBASHSCRIPTS=$PWD/example/deployment/docker/bash-scripts
export LOCALJVMBASHSCRIPTS=$PWD/example/deployment/local-jvm/bash-scripts
export RESTEXAMPLE=$PWD/example/example-services/rest-example
export EXAMPLESERVICES=$PWD/example/example-services/