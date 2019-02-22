#!/usr/bin/env bash

if [[ $# -lt 2 ]];
then
    echo "Usage: $0 FILE BUCKET"
    echo -e "\nUpload FILE to a bucket named BUCKET to a minio instance inside a container"
    exit 1;
fi

docker rm -f  minio mc >/dev/null 2>&1

# Copy file into this directory
cp $1 /tmp

FNAME=$(basename "$1")

# Start Docker container for Minio server
docker run -p 9000:9000 --name minio --rm -d -e "MINIO_ACCESS_KEY=Palisade_example" -e "MINIO_SECRET_KEY=test_access" minio/minio server /export >/dev/null

# Wait for minio to start
while [[ $(docker logs minio | wc -l) -lt 1 ]] ; do
    sleep 1
done

# Upload a file to the server
# Start a Docker container for running mc commands, using host's network
docker run -dt --entrypoint cat --name mc --net=host -v /tmp:/resources minio/mc >/dev/null

# Configure local endpoint inside the mc container
docker exec mc mc config host add local http://localhost:9000 Palisade_example test_access

# Make example bucket
docker exec mc mc mb local/$2

# Upload example file to bucket
docker exec mc mc cp /resources/$FNAME local/$2

# Teardown
docker rm -f mc >/dev/null
rm /tmp/$FNAME