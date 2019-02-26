#!/usr/bin/env bash

. ./example/deployment/multi-use/bash-scripts/setScriptPath.sh
java -cp $MULTIJVMEXAMPLE/multi-jvm-example-runner/target/multi-jvm-example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.MultiJvmExample "$EXAMPLESERVICES/resources/exampleObj_file1.txt"
