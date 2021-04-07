#!/bin/bash
set -e

# This should be run from the root directory of the project: Palisade/> ./scripts/buildGitbook.sh

echo "Installing gitbook plugins"
gitbook install

echo "Building gitbook"
gitbook build

echo "'Deploying' /_book build to /doc directory"
mv _book/* docs/
