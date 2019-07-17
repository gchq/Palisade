#!/bin/bash
export PALISADE_REST_CONFIG_PATH="example/deployment/local-k8s/configRest.json"
java -cp example/example-model/target/example-model-*-shaded.jar -Dpalisade.rest.config.path=example/deployment/local-k8s/configRest.json uk.gov.gchq.palisade.example.runner.RestExample /data/employee_file0.avro
