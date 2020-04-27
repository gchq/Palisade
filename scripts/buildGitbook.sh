#!/bin/bash

# This should be run from the root of the project: ./doc/scripts/buildGitbook.sh

set -e

echo "Installing gitbook"

gitbook install
gitbook build
