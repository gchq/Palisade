From tomcat:8.0-jre8-alpine as config-service

ADD bin/rest-config-service/ /usr/local/tomcat/webapps/config
ADD conf/bootstrapConfig.json /usr/local/tomcat/webapps/config/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=config-service \
               -Dpalisade.properties.app.title=rest-config-service \
               -Dpalisade.rest.bootstrap.path=/usr/local/tomcat/webapps/config/WEB-INF/bootstrapConfig.json"
