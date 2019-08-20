#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

export PALISADE_REST_BOOTSTRAP_PATH=".extract/Config/webapps/config/WEB-INF/classes/bootstrapConfig.json"

java -jar "$EXAMPLESERVICES"/example-rest-config-service/target/example-rest-config-service-*-executable.jar \
                -httpPort=8085 \
                -extractDirectory=.extract/Config \
                -Dpalisade.rest.basePath=config \
                -Dpalisade.properties.app.title=rest-config-service
