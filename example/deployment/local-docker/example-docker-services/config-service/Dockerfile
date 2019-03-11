From tomcat:8-jre8-alpine

RUN apk add --no-cache curl
ADD docker/bin/rest-config-service/ /usr/local/tomcat/webapps/config
ADD docker/setenv.sh /usr/local/tomcat/bin/
ADD docker/bootstrapConfig.json /usr/local/tomcat/webapps/config/WEB-INF/

