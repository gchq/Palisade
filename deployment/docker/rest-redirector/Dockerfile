From tomcat:8-jre11 as rest-redirector

ADD bin/rest-redirector /usr/local/tomcat/webapps/redirector/
ADD bin/configRest.json /usr/local/tomcat/webapps/redirector/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=redirector \
               -Dpalisade.properties.app.title=rest-redirector \
               -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/redirector/WEB-INF/configRest.json
