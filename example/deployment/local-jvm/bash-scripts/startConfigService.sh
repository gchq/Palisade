#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -jar $RESTEXAMPLE/example-rest-config-service/target/example-rest-config-service-*-executable.jar \
                -httpPort=8085 \
                -extractDirectory=.extract/Config \
                -Dpalisade.rest.bootstrap.path=.extract/Config/webapps/config/WEB-INF/classes/bootstrapConfig.json \
                -Dpalisade.rest.basePath=config \
                -Dpalisade.properties.app.title=rest-config-service