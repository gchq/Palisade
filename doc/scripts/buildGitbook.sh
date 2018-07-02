#!/bin/bash

# This should be run from the root of the project: ./doc/scripts/buildGitbook.sh

set -e

echo "Installing gitbook"

./doc/scripts/buildJavadoc.sh
gitbook install
gitbook build
