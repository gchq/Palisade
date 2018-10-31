#!/usr/bin/env bash

set -e

if [ "$TRAVIS_PULL_REQUEST" != 'false' ]; then
    echo "Running install script: mvn -q install -B -V"
    mvn -q install -B -V
fi
