FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
ADD docker/bin/example-model-*-shaded.jar /tmp/
ADD waitForHost.sh /tmp/

CMD ["/tmp/waitForHost.sh", "http://config-service:8080/config/v1/status", "java", "-cp", "/tmp/example-model-*-shaded.jar", "uk.gov.gchq.palisade.example.config.DistributedServices", "http://etcd:2379", "http://palisade-service:8080/palisade", "http://policy-service:8080/policy", "http://resource-service:8080/resource", "http://user-service:8080/user", "http://data-service:8080/data", "http://config-service:8080/config", "http://palisade-service:8080/palisade", "http://data-service:8080/data"]
