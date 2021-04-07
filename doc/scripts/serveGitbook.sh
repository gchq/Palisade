#!/bin/bash

# This should be run from the root of the project: Palisade/> ./scripts/serveGitbook.sh

set -e

echo "Serving gitbook"
gitbook serve
