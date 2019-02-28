#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
#java -cp $RESTEXAMPLE/example-runner/target/example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.RestExample "$EXAMPLESERVICES/resources/exampleObj_file1.txt"

java -cp $EXAMPLESERVICES/example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar uk.gov.gchq.palisade.example.hrdatagenerator.CreateData /var/tmp 1 1
#This method needs at least three arguments. The directory path to save the files in, the number of employee's to generate and the number of files to split those employees between
# outputs /var/tmp/file0.avro