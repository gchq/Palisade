#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -cp $EXAMPLESERVICES/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.config.LocalServices