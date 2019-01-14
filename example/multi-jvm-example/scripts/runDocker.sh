#!/usr/bin/env bash
set -e

# Check that the required network is present
NETWORKS=$(docker network ls -f 'name=palisadeexample' --format '{{.Name}}')
if [[ $(grep -c palisadeexample <(echo $NETWORKS)) -lt 1 ]]; then
    echo "Docker network \"palisadeexample\" is not present. Do you need to run the dockerComposeUp.sh script?"
    exit 1
fi

docker build -t multi-jvm-example-docker-runner ./example/multi-jvm-example/multi-jvm-example-docker-runner/
docker run --network=palisadeexample_default --rm multi-jvm-example-docker-runner
