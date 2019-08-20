#!/usr/bin/env bash

JAVA_OPTS="-Dpalisade.rest.basePath=resource \
                  -Dpalisade.properties.app.title=rest-resource-service \
                  -DPALISADE_REST_CONFIG_PATH=/usr/local/tomcat/webapps/resource/WEB-INF/configRest.json"
