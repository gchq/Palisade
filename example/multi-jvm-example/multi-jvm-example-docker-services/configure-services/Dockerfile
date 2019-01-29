FROM openjdk:8-alpine

ADD docker/example-model-*-shaded.jar /tmp/

ENTRYPOINT [ "java" ]
CMD -cp /tmp/example-model-*-shaded.jar uk.gov.gchq.palisade.example.config.DistributedServicesConfigurator "http://etcd:2379", "http://palisade-service:8080/palisade", "http://policy-service:8080/policy", "http://resource-service:8080/resource", "http://user-service:8080/user", "http://data-service:8080/data", "http://config-service:8080/config"