#!/usr/bin/env bash

java -jar example/multi-jvm-example/multi-jvm-example-rest-config-service/target/multi-jvm-example-rest-config-service-*-executable.jar \
                -httpPort=8085 \
                -extractDirectory=.extract/Config \
                -Dpalisade.rest.bootstrap.path=.extract/Config/webapps/config/WEB-INF/classes/bootstrapConfig.json \
                -Dpalisade.rest.basePath=config \
                -Dpalisade.properties.app.title=rest-config-service