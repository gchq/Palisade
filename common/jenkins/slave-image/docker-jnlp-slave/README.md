<!--
/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
-->
# Docker-jnlp-slave image

This image uses jenkins/slave latest-jdk11 image released on 16th August 2019 from this repo: [repo](https://hub.docker.com/r/jenkins/slave/builds)
It is based on the Dockerfile extracted from here [link](https://github.com/jenkinsci/docker-jnlp-slave/blob/master/Dockerfile) and adds helm and tiller functionality.


## running on an EC2 instance
Ensure docker is installed and the aws-cli
```bash
make
sudo chmod 666 /var/run/docker.sock
docker run -v /var/run/docker.sock:/var/run/docker.sock --net=host -i -t <<image name>> sh
```

