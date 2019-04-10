#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

java -jar "$EXAMPLESERVICES"/example-rest-palisade-service/target/example-rest-palisade-service-*-executable.jar \
                -httpPort=8080 \
                -extractDirectory=.extract/Palisade \
                -Dpalisade.rest.config.path="$EXAMPLE/example-model/src/main/resources/configRest.json" \
                -Dpalisade.rest.basePath=palisade \
                -Dpalisade.properties.app.title=rest-palisade-service