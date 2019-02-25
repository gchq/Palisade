#!/usr/bin/env bash

java -cp example/example-services/example-model/target/example-model-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.config.ExampleConfigurator ./example/example-services/resources/exampleObj_file1.txt