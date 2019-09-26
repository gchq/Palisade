FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
ADD docker/bin/example-model-*-shaded.jar /tmp/
ADD waitForHost.sh /tmp/
ADD docker/bin/configRest.json /tmp/

ENV PALISADE_REST_CONFIG_PATH="/tmp/configRest.json"

CMD ["/tmp/waitForHost.sh", "http://policy-service:8080/policy/v1/status", "java", "-cp", "/tmp/example-model-*-shaded.jar", "uk.gov.gchq.palisade.example.config.ExampleConfigurator", "/data"]
