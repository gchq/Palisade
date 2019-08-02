#!/usr/bin/env bash

set -e

if [ "$TRAVIS_BRANCH" != 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    echo "Running install script: mvn install -q -B -V -P quick"
    time mvn install -q -B -V -P quick
fi
