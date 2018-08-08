#!/usr/bin/env bash
export JAVA_OPTS="-Dpalisade.rest.basePath=resource \
                  -Dpalisade.properties.app.title=rest-resource-service \
                  -Dpalisade.rest.resource.service.config.path=/usr/local/tomcat/webapps/resource/WEB-INF/resourceConfig.json"
