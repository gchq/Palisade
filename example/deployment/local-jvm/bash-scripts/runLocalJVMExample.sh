#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
java -cp $PWD/example/deployment/local-jvm/example-runner/target/example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.RestExample "$PWD/example/resources/exampleObj_file1.txt"
