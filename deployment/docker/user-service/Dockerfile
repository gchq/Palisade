From tomcat:8.0-jre8-alpine

ADD bin/rest-user-service /usr/local/tomcat/webapps/user/
ADD conf/userConfig.json /usr/local/tomcat/webapps/user/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=user \
               -Dpalisade.properties.app.title=rest-user-service \
               -Dpalisade.rest.user.service.config.path=/usr/local/tomcat/webapps/user/WEB-INF/userConfig.json"
