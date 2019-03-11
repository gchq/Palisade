#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
java -cp $EXAMPLESERVICES/example-rest-redirector-service/target/example-rest-redirector-service-*-shaded.jar \
                -Dpalisade.rest.basePath="http://localhost:8080/alter/this/path" \
                -Dpalisade.rest.config.path="$PWD/example/example-model/src/main/resources/configRest.json" \
        uk.gov.gchq.palisade.redirect.Launcher
