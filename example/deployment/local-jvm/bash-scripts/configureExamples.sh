#!/usr/bin/env bash

. ./example/deployment/multi-use/bash-scripts/setScriptPath.sh
java -cp $EXAMPLESERVICES/example-model/target/example-model-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.config.ExampleConfigurator $EXAMPLESERVICES/resources/exampleObj_file1.txt

