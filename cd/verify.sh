#!/usr/bin/env bash

. ./cd/verfiy_example.sh --source-only

set -e

result=0

if [ "$TRAVIS_PULL_REQUEST" != 'false' ]; then
    echo "Running verify script: mvn -q verify -P analyze -B"
#    mvn -q verify -P analyze -B
    echo "Running verify script: mvn -q verify -P test -B"
#    mvn -q verify -P test -B
    echo "Starting the multi-jvm-example containerised"
    ./example/multi-jvm-example/scripts/dockerComposeUp.sh
    #Sleep to ensure all containers are up
    sleep 10
    echo "Running the example application"
    OUTPUT=`./example/multi-jvm-example/scripts/run.sh`
    validate_example_output "$OUTPUT"
    result=$?
    echo $result
    echo "Stopping the multi-jvm-example containers"
    ./example/multi-jvm-example/scripts/dockerComposeDown.sh
    echo "Compiling javadoc"
    #mvn -q javadoc:aggregate -P quick
    
fi

exit $result




