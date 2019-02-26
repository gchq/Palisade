FROM quay.io/coreos/etcd

RUN apk add --no-cache curl

CMD ["/usr/local/bin/etcd", "-name", "etcd", "--data-dir", "/tmp/etcd_data", "-advertise-client-urls", "http://etcd:2379", "-listen-client-urls", "http://0.0.0.0:2379", "-initial-advertise-peer-urls", "http://etcd:2380", "-listen-peer-urls", "http://0.0.0.0:2380", "-initial-cluster", "etcd=http://etcd:2380"]