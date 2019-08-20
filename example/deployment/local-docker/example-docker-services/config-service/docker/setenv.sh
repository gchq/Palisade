#!/usr/bin/env bash

JAVA_OPTS="-Dpalisade.rest.basePath=config \
                  -Dpalisade.properties.app.title=rest-config-service \
                  -DPALISADE_REST_BOOTSTRAP_PATH=/usr/local/tomcat/webapps/config/WEB-INF/bootstrapConfig.json"
