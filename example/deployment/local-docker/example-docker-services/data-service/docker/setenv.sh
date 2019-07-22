#!/usr/bin/env bash

JAVA_OPTS="-Dpalisade.rest.basePath=data \
                  -Dpalisade.properties.app.title=rest-data-service \
                  -DPALISADE_REST_CONFIG_PATH=/usr/local/tomcat/webapps/data/WEB-INF/configRest.json"
