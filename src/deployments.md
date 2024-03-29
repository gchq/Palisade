<!---
Copyright 2018-2021 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->

# Deployments

## How might the system be deployed?

### Kubernetes (Locally or AWS EKS)

You will need:
* [Docker v19.03+](https://www.docker.com/) for building containers
* [Kubernetes v1.21+](https://kubernetes.io/) for cluster orchestration and container management
* [Helm v3+](https://v3.helm.sh/) for deploying to Kubernetes and managing deployments

![Palisade K8s Deployment](../img/K8s-Deployment.png)

\* _Service in this case means K8s `Service` resource, which acts as an in-cluster DNS name._

Palisade primarily supports deployment through `helm` to a K8s cluster.
This allows for containerisation, scaling and auto-recovery on service failure.
Palisade is regularly deployed to local K8s clusters for manual developer testing, and to AWS EKS for CI/CD automated testing.

In this deployment, containers for each microservice are managed by various K8s workload resources, notably `Deployment` and `Statefulset`.
These can then be scaled up or down as appropriate, with the optional metrics-server responsible for horizontal auto-scaling.

The Palisade services are accessed through a Traefik ingress, which is installed into the `kube-system` namespace rather than with the rest of the Palisade services (the `palisade` namespace in this diagram).
Each microservice is responsible for its own volume mounts, configuration, and ingress-route if any.

Redis and Kafka can be either installed into the same namespace as the Palisade services, or in a separate namespace not managed by Palisade (the `shared-infra` in this diagram).

Palisade can be configured via configuration yaml files, to use different clients, and therefore work with different technologies, see [Palisade Clients](palisade_clients.md).  
Further configuration can then point the Data and Resource Service to different data stores which can be located outside of the Palisade deployment.


### JVM Processes

You will need:
* [OpenJDK Java 11](https://openjdk.java.net/projects/jdk/11/) or [Oracle Java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
* [Apache Kafka](https://kafka.apache.org/downloads)
* [Redis](https://redis.io/download)


![Palisade JVM Deployment](../img/JVM-Deployment.png)

Palisade also supports running as bare JVM processes outside of K8s, but of course without any of the benefits provided by K8s.

In this deployment, a separate JVM is spawned for each microservice and will run unmanaged.
This means services will not recover from crashes or critical errors.

The Palisade services are accessed through their localhost address and port.
Each microservice uses the local filesystem without any additional volume mounts.

Palisade can be configured via environment variables passed to the jars, to use different clients, and therefore work with different technologies, see [Palisade Clients](palisade_clients.md).  
Further configuration can then point the Data and Resource Service to different data stores which can be located outside of the Palisade deployment.

_n.b. Redis and Kafka are still required in some form, whether exposed in a local K8s cluster, running as local processes, or hosted externally as a SaaS_.
