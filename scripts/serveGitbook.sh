#!/bin/bash

# This should be run from the root of the project: ./doc/scripts/serveGitbook.sh

set -e

echo "Serving gitbook"
gitbook serve
