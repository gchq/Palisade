#!/usr/bin/env bash
export JAVA_OPTS="-Dpalisade.rest.basePath=palisade \
                  -Dpalisade.properties.app.title=rest-palisade-service \
                  -Dpalisade.rest.service.config.path=/usr/local/tomcat/webapps/palisade/WEB-INF/palisadeConfig.json"
