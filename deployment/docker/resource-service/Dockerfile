From tomcat:8.0-jre8-alpine

ADD bin/rest-resource-service /usr/local/tomcat/webapps/resource/
ADD bin/configRest.json /usr/local/tomcat/webapps/resource/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=resource \
               -Dpalisade.properties.app.title=rest-resource-service \
               -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/resource/WEB-INF/configRest.json
