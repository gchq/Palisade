#!/usr/bin/env bash

java -jar example/multi-jvm-example/multi-jvm-example-rest-policy-service/target/multi-jvm-example-rest-policy-service-*-executable.jar -httpPort 8081 -extractDirectory=.extract/Policy -Dpalisade.rest.config.path=example/example-model/src/main/resources/configRest.json
