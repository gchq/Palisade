#!/bin/bash

# This should be run from the root directory of the project: Palisade/> ./scripts/buildGitbook.sh

set -e

echo "Installing gitbook"

gitbook install
gitbook build
mv _book/* doc/
