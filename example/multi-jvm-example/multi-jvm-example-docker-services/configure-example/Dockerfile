FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
ADD docker/bin/example-model-*-shaded.jar /tmp/
ADD waitForHost.sh /tmp/
ADD docker/bin/configRest.json /tmp/

CMD ["/tmp/waitForHost.sh", "http://policy-service:8080/policy/v1/status", "java", "-cp", "/tmp/example-model-*-shaded.jar", "-Dpalisade.rest.config.path=/tmp/configRest.json", "uk.gov.gchq.palisade.example.config.ExampleConfigurator", "/data/exampleObj_file1.txt"]