FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
COPY docker/bin/multi-jvm-example-rest-redirector-*-shaded.jar /multi-jvm-example-rest-redirector/
ADD docker/bin/configRest.json /multi-jvm-example-rest-redirector/

ENTRYPOINT ["java"]
CMD ["-cp", "/multi-jvm-example-rest-redirector/*", "-Dpalisade.rest.basePath=http://0.0.0.0:8080/data/v1", "-Dpalisade.rest.config.path=/multi-jvm-example-rest-redirector/configRest.json", "uk.gov.gchq.palisade.redirect.Launcher"]
