#!/usr/bin/env bash

JAVA_OPTS="-Dpalisade.rest.basePath=palisade \
                  -Dpalisade.properties.app.title=rest-palisade-service \
                  -DPALISADE_REST_CONFIG_PATH=/usr/local/tomcat/webapps/palisade/WEB-INF/configRest.json"
