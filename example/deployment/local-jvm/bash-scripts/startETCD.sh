#!/usr/bin/env bash

ETCD_VER=v3.3.12

docker rm etcd-gcr-${ETCD_VER}
rm -rf /tmp/etcd-data.tmp
mkdir -p /tmp/etcd-data.tmp
docker rmi gcr.io/etcd-development/etcd:${ETCD_VER} || true
docker run \
  -p 31379:2379 \
  -p 31380:2380 \
  -d \
  --mount type=bind,source=/tmp/etcd-data.tmp,destination=/etcd-data \
  --name etcd-gcr-${ETCD_VER} \
  gcr.io/etcd-development/etcd:${ETCD_VER} \
  /usr/local/bin/etcd \
  --name s1 \
  --data-dir /etcd-data \
  --listen-client-urls http://0.0.0.0:2379 \
  --advertise-client-urls http://0.0.0.0:2379 \
  --listen-peer-urls http://0.0.0.0:2380 \
  --initial-advertise-peer-urls http://0.0.0.0:2380 \
  --initial-cluster s1=http://0.0.0.0:2380 \
  --initial-cluster-token tkn \
  --initial-cluster-state new