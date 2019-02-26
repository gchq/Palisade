#!/usr/bin/env bash

. ./example/deployment/multi-use/bash-scripts/setScriptPath.sh
java -cp $MULTIJVMEXAMPLE/multi-jvm-example-rest-redirector/target/multi-jvm-example-rest-redirector-*-shaded.jar \
                -Dpalisade.rest.basePath="http://localhost:8080/alter/this/path" \
                -Dpalisade.rest.config.path="$EXAMPLESERVICES/example-model/src/main/resources/configRest.json" \
        uk.gov.gchq.palisade.redirect.Launcher
