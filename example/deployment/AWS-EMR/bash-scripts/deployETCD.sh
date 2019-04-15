#!/bin/bash

# This script assumes it is running on the master node of an AWS EMR cluster from the hadoop users home directory
#
# This script will create an etcd cluster over all of the slave/core Yarn nodes,
# persisting the data in 'etcd_<ip address>.etcd'

if [ $# -gt 0 ]; then
    # need to pass in the location of your .pem file
    key=$1

    if [ ${key: -4} == ".pem" ]; then
        # download etcd
        ETCD_VER=v3.3.10

        DOWNLOAD_URL=https://storage.googleapis.com/etcd

        rm -f /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz
        sudo rm -rf /opt/etcd-${ETCD_VER}
        sudo mkdir -p /opt/etcd-${ETCD_VER}

        curl -L ${DOWNLOAD_URL}/${ETCD_VER}/etcd-${ETCD_VER}-linux-amd64.tar.gz -o /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz
        sudo tar xzvf /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz -C /opt/etcd-${ETCD_VER} --strip-components=1
        rm -f /tmp/etcd-${ETCD_VER}-linux-amd64.tar.gz

        # identify all the yarn nodes in the cluster
        declare nodes=`yarn node -list 2> /dev/null | grep internal | cut -c 4- | tr '.' '\t' | cut -f1 | tr '-' '.'`

        # create the -initial-cluster string
        for node in $nodes
        do
           if [ -z "${initial_cluster_string}" ]; then
             initial_cluster_string="etcd_$node=http://$node:2380"
           else
             initial_cluster_string="${initial_cluster_string},etcd_$node=http://$node:2380"
           fi
        done

        for node in $nodes
         do
           ssh -f -i $key -o StrictHostKeyChecking=no hadoop@$node "rm -rf /tmp/etcd-${ETCD_VER}"
           scp -i $key -r -q /opt/etcd-${ETCD_VER} hadoop@$node:/tmp/
           ssh -f -i $key -o StrictHostKeyChecking=no hadoop@$node "sudo rm -rf /opt/etcd-${ETCD_VER}; \
                                                                    sudo mkdir -p /opt/etcd-${ETCD_VER}; \
                                                                    sudo mv /tmp/etcd-${ETCD_VER} /opt"

           ssh -f -i $key -o StrictHostKeyChecking=no hadoop@$node "sudo /opt/etcd-${ETCD_VER}/etcd \
           --name=etcd_$node \
           --initial-advertise-peer-urls=http://$node:2380 \
           --listen-peer-urls=http://$node:2380 \
           --listen-client-urls=http://$node:2379,http://127.0.0.1:2379 \
           --advertise-client-urls=http://$node:2379 \
           --initial-cluster-token=etcd-cluster-1 \
           --initial-cluster=${initial_cluster_string} \
           --initial-cluster-state=new"
         done
    else
        echo "The first argument should be to a .pem file."
    fi
else
    echo "Your command line contains no arguments, you need to supply the .pem file associated with your emr cluster."
fi
