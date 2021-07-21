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

# <img src="logos/logo.svg" width="180">

## Getting started

### Prerequisites
Before running, make sure you have installed and appropriately-configured the following:
* [Git](https://git-scm.com/downloads)
* [OpenJDK Java 11](https://openjdk.java.net/projects/jdk/11/) or [Oracle Java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
* [Docker](https://www.docker.com/products/docker-desktop) and Kubernetes, which now is bundled with Docker Desktop. You may be required to create a Docker account and login to successfully pull images from DockerHub
* [Helm 3](https://helm.sh/) for deploying to Kubernetes and managing deployments

## Running the Quickstart Script
Run the cross-platform [quickstart.cmd script](quickstart.cmd):
* On Linux/MacOS:
  ```
  /dev/Palisade> bash quickstart.cmd
  ```
* On Windows:
  ```
  C:\dev\Palisade> quickstart.cmd
  ```

This will perform the following tasks necessary to set-up and start using Palisade:
* Download each of the Palisade repos required to run the example ([services](https://github.com/gchq/Palisade-services/tree/develop) and [examples](https://github.com/gchq/Palisade-examples/tree/develop))
    - We will be pulling the 0.5.0 release which uses a Kafka-based streaming microservice architecture
* Run the Palisade local-k8s example (more details [can be found here](https://github.com/gchq/Palisade-examples/tree/develop/deployment/local-k8s)), which pulls down the images from [DockerHub](https://hub.docker.com/u/gchq).

The script will have done an example run-through of Palisade, demonstrating a client with different users and purposes querying some Avro files for employee data, with some redaction and masking rules in place.
The output of this example run-through will be written to the terminal once it has completed, and if deployed successfully, you will see a success message returned.
To view the logs of the services, use Kubernetes and run `kubectl get logs <ServiceName>-Service`. 
More details of these rules and data structures [can be found here](https://github.com/gchq/Palisade-examples/tree/develop/example-library).

This shutdown procedure can be automated using the [quickstop.cmd script](quickstop.cmd):
* On Linux/MacOS:
  ```
  /dev/Palisade> bash quickstop.cmd
  ```
* On Windows:
  ```
  C:\dev\Palisade> quickstop.cmd
  ```
See the individual repositories and modules for their specific documentation from this point.

## Alternative Deployments

### Local JVM
Palisade is also set-up for a JVM deployment, which [is documented here](https://github.com/gchq/Palisade-examples/tree/develop/deployment/local-jvm/README.md).  
To run this example, you will need local installations of each service's jars, which involves cloning the remaining Palisade repositories, and using [maven](https://maven.apache.org/), installing them in the correct order, before following the local-jvm readme linked above.  
The correct installation order for Palisade is:
1. [Palisade-common](https://github.com/gchq/Palisade-common)
1. [Palisade-clients](https://github.com/gchq/Palisade-clients)
1. [Palisade-services](https://github.com/gchq/Palisade-services)
1. [Palisade-readers](https://github.com/gchq/Palisade-readers)
1. [Palisade-examples](https://github.com/gchq/Palisade-examples)
