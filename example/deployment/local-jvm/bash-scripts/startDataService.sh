#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

java -jar "$EXAMPLESERVICES"/example-rest-data-service/target/example-rest-data-service-*-executable.jar \
                -httpPort=8084 \
                -extractDirectory=.extract/Data \
                -Dpalisade.rest.config.path="$EXAMPLE/example-model/src/main/resources/configRest.json" \
                -Dpalisade.properties.app.title=rest-data-service \
                -Dpalisade.rest.basePath=data \
				-Droot.log.level=debug
