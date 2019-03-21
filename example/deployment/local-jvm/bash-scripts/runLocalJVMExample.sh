#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
java -cp $PWD/example/deployment/local-jvm/example-runner/target/example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.RestExample "$EXAMPLE/resources/exampleEmployee_file0.avro"








