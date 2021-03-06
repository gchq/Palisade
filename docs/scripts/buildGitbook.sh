#!/bin/bash
set -e

# This should be run from the root directory of the project: Palisade/> ./scripts/buildGitbook.sh

echo "Installing gitbook plugins"
gitbook install

echo "Cleaning build directory"
rm -rf _book
rm -rf docs

echo "Building gitbook"
gitbook build

echo "'Deploying' /_book build to /docs directory"
mkdir docs
mv -u _book/* docs
