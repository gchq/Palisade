#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="$EXAMPLE/example-model/src/main/resources/configRest.json"

java -cp "$EXAMPLESERVICES"/example-rest-redirector-service/target/example-rest-redirector-service-*-shaded.jar \
                -Dpalisade.rest.basePath="http://localhost:8080/alter/this/path" \
        uk.gov.gchq.palisade.redirect.service.Launcher
