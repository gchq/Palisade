#!/usr/bin/env bash
export JAVA_OPTS="-Dpalisade.rest.basePath=config-service \
                  -Dpalisade.properties.app.title=rest-config-service \
                  -Dpalisade.rest.config.service.config.path=/usr/local/tomcat/webapps/config/WEB-INF/configserviceConfig.json"
