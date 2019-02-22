From tomcat:8-jre11 as user-service

ADD bin/rest-user-service /usr/local/tomcat/webapps/user/
ADD bin/configRest.json /usr/local/tomcat/webapps/user/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=user \
               -Dpalisade.properties.app.title=rest-user-service \
               -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/user/WEB-INF/configRest.json
