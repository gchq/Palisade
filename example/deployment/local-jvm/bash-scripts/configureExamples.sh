#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
java -cp $PWD/example/example-model/target/example-model-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.config.ExampleConfigurator $PWD/example/resources/exampleObj_file1.txt

