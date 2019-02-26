#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -cp $RESTEXAMPLE/example-runner/target/example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.RestExample "$EXAMPLESERVICES/resources/exampleObj_file1.txt"
