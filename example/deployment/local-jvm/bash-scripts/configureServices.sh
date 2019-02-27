#!/usr/bin/env bash
#populate the config service with the addresses of all the other services, requires the config service to be running

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -cp $EXAMPLESERVICES/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.config.LocalServices