#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="$EXAMPLE/example-model/src/main/resources/configRest.json"

java -jar "$EXAMPLESERVICES"/example-rest-policy-service/target/example-rest-policy-service-*-executable.jar \
                -httpPort=8081 \
                -extractDirectory=.extract/Policy \
                -Dpalisade.rest.basePath=policy \
                -Dpalisade.properties.app.title=rest-policy-service
