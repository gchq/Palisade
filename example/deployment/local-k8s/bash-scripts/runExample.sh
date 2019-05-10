#!/bin/bash
java -cp example/deployment/local-jvm/example-runner/target/example-runner-*-shaded.jar -Dpalisade.rest.config.path=example/deployment/local-k8s/configRest.json uk.gov.gchq.palisade.example.RestExample /data/employee_file0.avro
