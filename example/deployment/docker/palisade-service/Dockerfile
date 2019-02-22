From tomcat:8-jre11 as palisade-service

ADD bin/rest-palisade-service /usr/local/tomcat/webapps/palisade
ADD bin/configRest.json /usr/local/tomcat/webapps/palisade/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=palisade \
               -Dpalisade.properties.app.title=rest-palisade-service \
               -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/palisade/WEB-INF/configRest.json
