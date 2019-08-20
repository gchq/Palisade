#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="$EXAMPLE/example-model/src/main/resources/configRest.json"

java -jar "$EXAMPLESERVICES"/example-rest-user-service/target/example-rest-user-service-*-executable.jar \
                -httpPort=8083 \
                -extractDirectory=.extract/User \
                -Dpalisade.rest.basePath=user \
                -Dpalisade.properties.app.title=rest-user-service
