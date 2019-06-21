#!/bin/bash

# This script assumes it is running on the master node of an AWS EMR cluster from the hadoop users home directory
#
# This script will create an etcd cluster over all of the slave/core Yarn nodes,
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

sudo /opt/etcd-${ETCD_VER}/etcd \
  --name=etcd_s1 \
  --data-dir /etcd-data \
  #--initial-advertise-peer-urls=http://$node:2380 \
  --listen-client-urls=http://0.0.0.0:2379 \
  --advertise-client-urls=http://0.0.0.0:2379 \
  --listen-peer-urls=http://0.0.0.0:2380 \
  --initial-advertise-peer-urls http://0.0.0.0:2380 \
  --initial-cluster=etcd_s1=http://0.0.0.0:2380 \
  --initial-cluster-token=tkn \
  --initial-cluster-state=new



#  /usr/local/bin/etcd \
#  --name s1 \
#  --data-dir /etcd-data \
#  --listen-client-urls http://0.0.0.0:2379 \
#  --advertise-client-urls http://0.0.0.0:2379 \
#  --listen-peer-urls http://0.0.0.0:2380 \
#  --initial-advertise-peer-urls http://0.0.0.0:2380 \
#  --initial-cluster s1=http://0.0.0.0:2380 \
#  --initial-cluster-token tkn \
#  --initial-cluster-state new