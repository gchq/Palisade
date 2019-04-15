#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

java -cp "$EXAMPLESERVICES"/example-rest-redirector-service/target/example-rest-redirector-service-*-shaded.jar \
                -Dpalisade.rest.basePath="http://localhost:8080/alter/this/path" \
                -Dpalisade.rest.config.path="$EXAMPLE/example-model/src/main/resources/configRest.json" \
        uk.gov.gchq.palisade.redirect.Launcher
