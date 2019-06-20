#!/usr/bin/env bash

. ./cd/verify_example.sh --source-only

set -e

container_result=0
multi_jvm_result=0
multi_jvm_cat_client_result=0

if [ "$TRAVIS_PULL_REQUEST" != 'false' ]; then
    echo "Building Palisade code: mvn install -q -B -V -P error-prone"
    mvn install -q -B -V -P error-prone
    ./example/deployment/local-jvm/bash-scripts/buildServices.sh
    export PALISADE_REST_CONFIG_PATH="$(pwd)/example/example-model/src/main/resources/configRest.json"

    echo "Starting the local-docker-example containers"
    ./example/deployment/local-docker/bash-scripts/dockerComposeUp.sh
    # Sleep to allow containers to start
    sleep 120s
    echo "Running the example application"
    OUTPUT=`./example/deployment/local-docker/bash-scripts/runLocalDockerExample.sh | tee /dev/tty`
    echo "Output is: $OUTPUT"
    validate_example_output "$OUTPUT"
    container_result=$?
    echo "Stopping the local-docker-example containers"
    echo "y" | ./example/deployment/local-docker/bash-scripts/dockerCleanSystem.sh

    echo "Starting the local-jvm-example"
    ./example/deployment/local-jvm/bash-scripts/startAllServices.sh
    echo "Running the example application"
    OUTPUT=`./example/deployment/local-jvm/bash-scripts/runLocalJVMExample.sh | tee /dev/tty`
    echo "Output is: $OUTPUT"
    validate_example_output "$OUTPUT"
    multi_jvm_result=$?

    OUTPUT=`java -cp $(pwd)/client-impl/cat-client/target/cat-client-*-shaded.jar uk.gov.gchq.palisade.client.CatClient Alice file://$(pwd)/example/resources/employee_file0.avro SALARY | tee /dev/tty`
    echo "Output is: $OUTPUT"
    validate_example_output "$OUTPUT"
    multi_jvm_cat_client_result=$?
    echo "Stopping the local-jvm-example"
    ./example/deployment/local-jvm/bash-scripts/stopAllServices.sh

    echo "Compiling javadoc"
    mvn javadoc:aggregate -q
fi
if [[ ${container_result} -eq 0 ]] && [[ ${multi_jvm_result} -eq 0 ]] && [[ ${multi_jvm_cat_client_result} -eq 0 ]]; then
    echo "exit 0"
    exit 0
else
    echo "exit 1"
    exit 1
fi




