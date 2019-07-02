#!/bin/bash

# This script will create an etcd instance
# persisting the data in 'etcd_<ip address>.etcd'

# download etcd
ETCD_VER=v3.3.10
DOWNLOAD_URL=https://storage.googleapis.com/etcd

rm -f /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz
sudo rm -rf /opt/etcd-${ETCD_VER}
sudo mkdir -p /opt/etcd-${ETCD_VER}

curl -L ${DOWNLOAD_URL}/${ETCD_VER}/etcd-${ETCD_VER}-linux-amd64.tar.gz -o /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz
sudo tar xzvf /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz -C /opt/etcd-${ETCD_VER} --strip-components=1
rm -f /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz

#private_ip=`hostname -I  | sed 's/ .*//'`
#cat deployETCD.sh | sed "s/PRIVATEIP/$ip/"

sudo /opt/etcd-${ETCD_VER}/etcd \
  --name=etcd_s1 \
  --data-dir /etcd-data \
  --initial-advertise-peer-urls=http://PRIVATEIP:2380 \
  --listen-client-urls=http://PRIVATEIP:2379 \
  --advertise-client-urls=http://PRIVATEIP:2379 \
  --listen-peer-urls=http://PRIVATEIP:2380 \
  --initial-advertise-peer-urls=http://PRIVATEIP:2380 \
  --initial-cluster=etcd_s1=http://PRIVATEIP:2380 \
  --initial-cluster-token=tkn \
  --initial-cluster-state=new
