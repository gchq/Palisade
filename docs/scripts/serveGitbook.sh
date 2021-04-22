#!/bin/bash

# This should be run from the root of the project: Palisade/> ./scripts/serveGitbook.sh

set -e

echo "Installing gitbook plugins"
gitbook install

echo "Serving gitbook"
gitbook serve
