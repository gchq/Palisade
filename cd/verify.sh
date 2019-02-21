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
    ./example/multi-jvm-example/scripts/dockerCleanSystem.sh

    echo "Starting the multi-jvm-example"
    ./example/multi-jvm-example/scripts/startAllServices.sh
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
    echo "exit 0"
    exit 0
else
    echo "exit 1"
    exit 1
fi




