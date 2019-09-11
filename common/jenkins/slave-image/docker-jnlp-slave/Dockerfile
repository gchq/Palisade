#
#
# Copyright 2019 Crown Copyright
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM openjdk:11
ARG VERSION=3.30
LABEL Description="This is an image, which provides the Jenkins agent executable (slave.jar)" Vendor="Jenkins project" Version="${VERSION}"

ARG user=jenkins
ARG group=jenkins
ARG uid=1000
ARG gid=1000
ARG gid_docker=1001

RUN groupadd -g ${gid} ${group}
RUN useradd -c "Jenkins user" -d /home/${user} -u ${uid} -g ${gid} -m ${user}

ARG AGENT_WORKDIR=/home/${user}/agent


RUN echo 'deb http://deb.debian.org/debian stretch-backports main' > /etc/apt/sources.list.d/stretch-backports.list

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install git-lfs && apt-get install jq --assume-yes apt-utils
RUN curl --create-dirs -fsSLo /usr/share/jenkins/slave.jar https://repo.jenkins-ci.org/public/org/jenkins-ci/main/remoting/${VERSION}/remoting-${VERSION}.jar \
  && chmod 755 /usr/share/jenkins \
  && chmod 644 /usr/share/jenkins/slave.jar

ENV AGENT_WORKDIR=${AGENT_WORKDIR}
RUN mkdir /home/${user}/.jenkins && mkdir -p ${AGENT_WORKDIR}

VOLUME /home/${user}/.jenkins
VOLUME ${AGENT_WORKDIR}
WORKDIR /home/${user}

################# install aws-cli #################
#install aws-cli N.B the creds required are found by running: curl http://169.254.169.254/latest/meta-data/iam/security-credentials/palisade-admin
RUN apt-get install vim -y
RUN apt-get install python3 -y
RUN apt-get install -y net-tools
RUN curl -O https://bootstrap.pypa.io/get-pip.py && python3 get-pip.py && pip3 install awscli --upgrade
RUN curl -o aws-iam-authenticator https://amazon-eks.s3-us-west-2.amazonaws.com/1.13.8/2019-08-14/bin/linux/amd64/aws-iam-authenticator \
    && chmod +x ./aws-iam-authenticator && mv ./aws-iam-authenticator /usr/local/bin/aws-iam-authenticator


################# install kubectl #################
RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.15.3/bin/linux/amd64/kubectl \
  && chmod +x ./kubectl && mv ./kubectl /usr/local/bin/kubectl

################# install helm #################
RUN curl -LO https://get.helm.sh/helm-v2.14.3-linux-amd64.tar.gz \
  && tar -zxvf helm-v2.14.3-linux-amd64.tar.gz && mv linux-amd64/helm /usr/local/bin/helm \
  && mv linux-amd64/tiller /usr/local/bin/tiller \
  && rm -rf helm-v2.14.3-linux-amd64.tar.gz

# You will need to configure $HELM_HOST to specify the location of the tiller host - this will be the host machine
# and will be running on port 8080

################# get docker #################
# we will not run docker - instead we will redirect the docker socket from the host machine by
# starting the container as follows:
# docker run -v /var/run/docker.sock:/var/run/docker.sock -i -t <<id>> sh
#RUN apt install docker.io


RUN curl -fsSLO https://download.docker.com/linux/static/stable/x86_64/docker-19.03.2.tgz \
 &&  tar --strip-components=1 -xvzf docker-19.03.2.tgz -C /usr/local/bin
# RUN chown -R ${user}:${user} /var/run/docker.sockdocker

################# finished with sudo so switch to jenkins user #################
USER ${user}
RUN mkdir -p /home/${user}/.kube
COPY jenkins-slave /usr/local/bin/jenkins-slave
COPY create-repository /usr/local/bin/create-repository


# On a MAC you can access the host ports on host.docker.internal
# see: https://docs.docker.com/docker-for-mac/networking/
# The following command can be run on a MAC: kubectl --server=http://host.docker.internal:8080 get pods
# This guide describes how to install tiller on the EC2 instance

ENTRYPOINT ["jenkins-slave"]
