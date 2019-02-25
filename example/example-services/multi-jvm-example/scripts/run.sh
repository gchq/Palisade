#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

#echo DIR is: $DIR

java -cp example/example-services/multi-jvm-example/multi-jvm-example-runner/target/multi-jvm-example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.MultiJvmExample "$DIR/../../resources/exampleObj_file1.txt"
