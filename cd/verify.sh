#!/usr/bin/env bash

. ./cd/verify_example.sh --source-only

set -e

result=0

if [ "$TRAVIS_PULL_REQUEST" != 'false' ]; then
    echo "check 0"
    ls -la /home/travis/build/gchq/Palisade/example/example-model/target/classes
    echo "Running verify script: mvn -q verify -P analyze -B"
    mvn -q verify -P analyze -B
    echo "check 1"
    ls -la /home/travis/build/gchq/Palisade/example/example-model/target/classes
    echo "Running verify script: mvn verify -P test -B"
    mvn -q verify -P test -B
    echo "check 2"
    ls -la /home/travis/build/gchq/Palisade/example/example-model/target/classes
    echo "Starting the multi-jvm-example containerised"
    ./example/multi-jvm-example/scripts/dockerComposeUp.sh
    #Sleep to ensure all containers are up
    sleep 2m
    echo "Running the example application"
    OUTPUT=`./example/multi-jvm-example/scripts/runDocker.sh`
    echo "Output is: $OUTPUT"
    validate_example_output "$OUTPUT"
    result=$?
    echo "Stopping the multi-jvm-example containers"
    ./example/multi-jvm-example/scripts/dockerComposeDown.sh
    echo "Compiling javadoc"
    echo "check 3"
    ls -la /home/travis/build/gchq/Palisade/example/example-model/target/classes
    mvn clean install -P quick -B
    mvn javadoc:aggregate -P quick -B

fi

exit $result




