#!/bin/bash
export PALISADE_REST_CONFIG_PATH="example/deployment/local-k8s/configRest.json"

java -cp example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.runner.RestExample /data/
