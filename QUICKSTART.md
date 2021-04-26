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
The repo comes bundled with the following:
* [Apache Maven 3.6.3](https://maven.apache.org/download.cgi) via `mvnw`

Before running, make sure you have installed and appropriately-configured the following:
* [Git](https://git-scm.com/downloads)
* [OpenJDK Java 11](https://openjdk.java.net/projects/jdk/11/) or [Oracle Java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
* [Apache Kafka](https://kafka.apache.org/downloads) and [Redis](https://redis.io/download) available at `localhost:9092` and `localhost:6379` respectively
  * for a quick-and-easy option, the Palisade-examples repo comes bundled with a [docker-compose file](https://github.com/gchq/Palisade-examples/tree/develop/deployment/local-jvm/docker-compose.yml) for Kafka, Zookeeper and Redis - `docker-compose -f Palisade-examples/deployment/local-jvm/docker-compose.yml up`

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
* Download each of the Palisade repos required to run the example ([common](https://github.com/gchq/Palisade-common/tree/develop), [clients](https://github.com/gchq/Palisade-clients/tree/develop), [services](https://github.com/gchq/Palisade-services/tree/develop), [readers](https://github.com/gchq/Palisade-readers/tree/develop), [examples](https://github.com/gchq/Palisade-examples/tree/develop))
    - Since Palisade remains in active development, we will be pulling the 0.5.0 release which uses a Kafka-based streaming microservice architecture
* Install each project in order of any dependencies
* Run the Palisade local-jvm example (more details [can be found here](https://github.com/gchq/Palisade-examples/tree/develop/deployment/local-jvm))

Once complete, you will have each of the Palisade projects cloned to your local machine.
Each Palisade module will be installed into your `~/.m2` cache and jars built to the appropriate `.../target` directories.
The services will be running locally in separate JVM processes.

The script will have done an example run-through of Palisade, demonstrating a client with different users and purposes querying some Avro files for employee data, with some redaction and masking rules in place.
The output of this example run-through will be written to the terminal once it has completed.
The logging output of all the services can be found in the `Palisade-services` directory.
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

### Kubernetes
Palisade is also set-up for a kubernetes deployment, which [is documented here](https://github.com/gchq/Palisade-examples/tree/develop/deployment/local-k8s).
Under this setup, the `Palisade-services` directory will need to be rebuilt with a `mvn install` (the `quickstart.cmd` script uses a `-Pquick` profile which skips docker image builds).
