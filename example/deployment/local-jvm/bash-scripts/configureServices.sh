#!/usr/bin/env bash
#populate the config service with the addresses of all the other services, requires the config service to be running

. ./example/deployment/bash-scripts/setScriptPath.sh
java -cp $PWD/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.config.LocalServices