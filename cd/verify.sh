#!/usr/bin/env bash

set -e

if [ "$TRAVIS_PULL_REQUEST" != 'false' ]; then
    echo "Running verify script: mvn -q verify -P analyze -B"
    mvn -q verify -P analyze -B
    echo "Running verify script: mvn -q verify -P test -B"
    mvn -q verify -P test -B
    echo "Compiling javadoc"
    mvn -q javadoc:aggregate -P quick
fi
