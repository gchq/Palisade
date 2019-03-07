#!/usr/bin/env bash

java -cp example/example-model/target/example-model-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.config.ExampleConfigurator ./example/resources/exampleObj_file1.txt