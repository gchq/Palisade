#!/bin/sh

set -e

host="$1"
shift
cmd="$@"

until $(curl --output /dev/null -sf $host); do
  >&2 echo "$host is unavailable - sleeping"
  sleep 1
done

>&2 echo "$host is up - executing command"
exec $cmd