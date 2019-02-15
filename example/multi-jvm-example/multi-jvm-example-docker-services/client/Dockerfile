FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
COPY docker/bin/multi-jvm-example-docker-runner-*-shaded.jar /multi-jvm-example-docker-runner/
ADD docker/bin/configRest.json /multi-jvm-example-docker-runner/

ENTRYPOINT ["java"]
CMD ["-cp", "/multi-jvm-example-docker-runner/*", "-Dpalisade.rest.config.path=/multi-jvm-example-docker-runner/configRest.json", "uk.gov.gchq.palisade.example.MultiJVMDockerExample"]
