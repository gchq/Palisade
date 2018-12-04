#!/usr/bin/env bash

java -jar example/multi-jvm-example/multi-jvm-example-rest-data-service/target/multi-jvm-example-rest-data-service-*-executable.jar -httpPort=8084 -extractDirectory=.extract/Data -Dpalisade.rest.config.path=example/example-model/src/main/resources/configRest.json

