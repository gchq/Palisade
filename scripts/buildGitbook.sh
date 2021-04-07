#!/bin/bash

# This should be run from the root directory of the project: Palisade/> ./scripts/buildGitbook.sh

set -e

echo "Installing gitbook plugins"
gitbook install

echo "Building gitbook"
gitbook build

echo "'Deploying' build to doc directory"
mv _book/* doc/
