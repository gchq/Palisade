#!/usr/bin/env bash

set -e

if [ "$TRAVIS_PULL_REQUEST" != 'false' ]; then
    echo "Running install script: mvn -q install -P quick,travis -B -V"
    mvn -q install -P quick -B -V
fi
