FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
COPY docker/bin/example-docker-runner-*-shaded.jar /example-docker-runner/
ADD docker/bin/configRest.json /example-docker-runner/

ENTRYPOINT ["java"]
CMD ["-cp", "/example-docker-runner/*", "-Dpalisade.rest.config.path=/example-docker-runner/configRest.json", "uk.gov.gchq.palisade.example.RestDockerExample", "/data/Employee_file0.avro"]
