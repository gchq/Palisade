#!/usr/bin/env bash

. ./cd/verify_example.sh --source-only

set -e

container_result=0
multi_jvm_result=0

if [ "$TRAVIS_PULL_REQUEST" != 'false' ]; then
    echo "Building Palisade code: mvn install -q -B -V"
    mvn install -q -B -V
    ./example/multi-jvm-example/scripts/buildServices.sh

    echo "Starting the multi-jvm-example containerised"
    ./example/multi-jvm-example/scripts/dockerComposeUp.sh
    # Sleep to allow containers to start
    sleep 5s
    echo "Running the example application"
    OUTPUT=`./example/multi-jvm-example/scripts/runDocker.sh | tee /dev/tty`
    echo "Output is: $OUTPUT"
    validate_example_output "$OUTPUT"
    container_result=$?
    echo "Stopping the multi-jvm-example containers"
    ./example/multi-jvm-example/scripts/dockerComposeDown.sh

    echo "Starting the multi-jvm-example"
    while ! ls example/multi-jvm-example/multi-jvm-example-rest-config-service/target/multi-jvm-example-rest-config-service-*-executable.jar 1> /dev/null 2>&1 ; do
        echo "Cannot find example/multi-jvm-example/multi-jvm-example-rest-config-service/target/multi-jvm-example-rest-config-service-*-executable.jar, sleeping for 1 second to wait for build services to finish"
        sleep 1
    done
    while ! ls example/multi-jvm-example/multi-jvm-example-rest-data-service/target/multi-jvm-example-rest-data-service-*-executable.jar 1> /dev/null 2>&1 ; do
        echo "Cannot find example/multi-jvm-example/multi-jvm-example-rest-data-service/target/multi-jvm-example-rest-data-service-*-executable.jar, sleeping for 1 second to wait for build services to finish"
        sleep 1
    done
    while ! ls example/multi-jvm-example/multi-jvm-example-rest-palisade-service/target/multi-jvm-example-rest-palisade-service-*-executable.jar 1> /dev/null 2>&1 ; do
        echo "Cannot find example/multi-jvm-example/multi-jvm-example-rest-palisade-service/target/multi-jvm-example-rest-palisade-service-*-executable.jar, sleeping for 1 second to wait for build services to finish"
        sleep 1
    done
    while ! ls example/multi-jvm-example/multi-jvm-example-rest-policy-service/target/multi-jvm-example-rest-policy-service-*-executable.jar 1> /dev/null 2>&1 ; do
        echo "Cannot find example/multi-jvm-example/multi-jvm-example-rest-policy-service/target/multi-jvm-example-rest-policy-service-*-executable.jar, sleeping for 1 second to wait for build services to finish"
        sleep 1
    done
    while ! ls example/multi-jvm-example/multi-jvm-example-rest-resource-service/target/multi-jvm-example-rest-resource-service-*-executable.jar 1> /dev/null 2>&1 ; do
        echo "Cannot find example/multi-jvm-example/multi-jvm-example-rest-resource-service/target/multi-jvm-example-rest-resource-service-*-executable.jar, sleeping for 1 second to wait for build services to finish"
        sleep 1
    done
    while ! ls example/multi-jvm-example/multi-jvm-example-rest-user-service/target/multi-jvm-example-rest-user-service-*-executable.jar 1> /dev/null 2>&1 ; do
        echo "Cannot find example/multi-jvm-example/multi-jvm-example-rest-user-service/target/multi-jvm-example-rest-user-service-*-executable.jar, sleeping for 1 second to wait for build services to finish"
        sleep 1
    done
    ./example/multi-jvm-example/scripts/startAllServices.sh
    # Sleep to allow services to start
    sleep 30s
    echo "Running the example application"
    OUTPUT=`./example/multi-jvm-example/scripts/run.sh | tee /dev/tty`
    echo "Output is: $OUTPUT"
    validate_example_output "$OUTPUT"
    multi_jvm_result=$?
    echo "Stopping the multi-jvm-example"
    ./example/multi-jvm-example/scripts/stopAllServices.sh

    echo "Compiling javadoc"
    mvn javadoc:aggregate -q
fi
if [ ${container_result} -eq 0 ] && [ ${multi_jvm_result} -eq 0 ]; then
    exit 0
else
    exit 1
fi




