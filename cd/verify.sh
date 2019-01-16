#!/usr/bin/env bash

. ./cd/verify_example.sh --source-only

set -e

result=0

if [ "$TRAVIS_PULL_REQUEST" != 'false' ]; then
    echo "Building Palisade code: mvn install -q -B -V"
    mvn install -q -B -V
    echo "Starting the multi-jvm-example containerised"
    ./example/multi-jvm-example/scripts/dockerComposeUp.sh
    # Sleep to allow containers to start
    sleep 5s
    echo "Running the example application"
    OUTPUT=`./example/multi-jvm-example/scripts/runDocker.sh`
    echo "Output is: $OUTPUT"
    validate_example_output "$OUTPUT"
    result=$?
    echo "Stopping the multi-jvm-example containers"
    ./example/multi-jvm-example/scripts/dockerComposeDown.sh
    echo "Compiling javadoc"
    mvn javadoc:aggregate -q
fi

exit $result




