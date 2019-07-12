#!/bin/bash

# add hadoop to docker group
sudo usermod -a -G docker hadoop

# kill any running Palisade processes
sudo kill `ps -aef | grep example-rest-.*-service | grep -v grep | awk '{print $2}'` || echo Killed

# install git and clone the Palisade repository
sudo yum -y install git
sudo mkdir /mnt1/repos
sudo chown hadoop:hadoop /mnt1/repos
cd /mnt1/repos
sudo rm -rf Palisade
git clone https://github.com/gchq/Palisade.git
cd Palisade
git checkout gh-114-aws-emr-deployment

#install maven
cd
wget apache.mirrors.nublue.co.uk/maven/maven-3/3.6.1/binaries/apache-maven-3.6.1-bin.tar.gz
rm -rf /home/hadoop/apache-maven-3.6.1/home/hadoop/apache-maven-3.6.1
tar zxf apache-maven-3.6.1-bin.tar.gz
export PATH=$PATH:/home/hadoop/apache-maven-3.6.1/bin:$JAVA_HOME

# install / build
cd /mnt1/repos/Palisade
mvn clean install -P example > /home/hadoop/example_logs/install.log
./example/deployment/local-jvm/bash-scripts/buildServices.sh > /home/hadoop/example_logs/buildServices.log
