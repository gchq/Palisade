#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
#java -cp $RESTEXAMPLE/example-runner/target/example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.RestExample "$EXAMPLESERVICES/resources/exampleObj_file1.txt"
#java -cp $EXAMPLESERVICES/example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar uk.gov.gchq.palisade.example.hrdatagenerator.CreateData /var/tmp 1 1
java -cp $PWD/example/deployment/local-jvm/example-runner/target/example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.RestExample "$EXAMPLE/resources/exampleEmployee_file0.avro"
