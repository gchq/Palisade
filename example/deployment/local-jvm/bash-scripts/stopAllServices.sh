#!/usr/bin/env bash

pkill -u $(whoami) -f 'example-rest-.*-service'
docker stop etcd-gcr-v3.3.12
