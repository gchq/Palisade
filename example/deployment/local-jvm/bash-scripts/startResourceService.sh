#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="$EXAMPLE/example-model/src/main/resources/configRest.json"

java -jar "$EXAMPLESERVICES"/example-rest-resource-service/target/example-rest-resource-service-*-executable.jar \
                -httpPort=8082 \
                -extractDirectory=.extract/Resource \
                -Dpalisade.rest.basePath=resource \
                -Dpalisade.properties.app.title=rest-resource-service
