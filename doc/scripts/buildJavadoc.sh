#!/bin/bash

# This should be run from the root of the project: ./doc/scripts/buildJavadoc.sh

set -e

echo "Building javadoc"

mvn clean install
mvn javadoc:aggregate
rm -rf doc/javadoc
mkdir doc/javadoc
mv target/site/apidocs/* doc/javadoc/
