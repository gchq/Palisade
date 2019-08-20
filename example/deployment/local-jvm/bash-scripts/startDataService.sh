#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="$EXAMPLE/example-model/src/main/resources/configRest.json"

java -jar "$EXAMPLESERVICES"/example-rest-data-service/target/example-rest-data-service-*-executable.jar \
                -httpPort=8084 \
                -extractDirectory=.extract/Data \
                -Dpalisade.properties.app.title=rest-data-service \
                -Dpalisade.rest.basePath=data
