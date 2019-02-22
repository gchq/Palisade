#!/usr/bin/env bash

java -cp example/multi-jvm-example/multi-jvm-example-rest-redirector/target/multi-jvm-example-rest-redirector-*-shaded.jar \
                -Dpalisade.rest.basePath="http://localhost:8080/alter/this/path" \
                -Dpalisade.rest.config.path="example/example-model/src/main/resources/configRest.json" \
        uk.gov.gchq.palisade.redirect.Launcher
