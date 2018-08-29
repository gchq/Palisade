FROM alpine/git as clone

WORKDIR /app
RUN git clone https://github.com/gchq/Palisade.git
WORKDIR /app/Palisade

FROM maven:3.5-jdk-8-alpine as build

WORKDIR /app
COPY --from=clone /app/Palisade /app
RUN mvn clean install -Pquick;

ENTRYPOINT cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-data-service/target/multi-jvm-example-rest-data-service-*/* /data-vol/bin/rest-data-service/; \
           cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-palisade-service/target/multi-jvm-example-rest-palisade-service-*/* /palisade-vol/bin/rest-palisade-service/; \
           cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-policy-service/target/multi-jvm-example-rest-policy-service-*/* /policy-vol/bin/rest-policy-service/; \
           cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-resource-service/target/multi-jvm-example-rest-resource-service-*/* /resource-vol/bin/rest-resource-service/; \
           cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-user-service/target/multi-jvm-example-rest-user-service-*/* /user-vol/bin/rest-user-service/


