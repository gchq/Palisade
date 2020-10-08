
<!---
Copyright 2020 Crown Copyright

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

The repo comes bundled with the following:
* [Apache Maven 3.6.3](https://maven.apache.org/download.cgi) via `mvnw`

## Running the Quickstart Script
Run the cross-platform [quickstart.cmd script](quickstart.cmd):
* On Linux/MacOS:
  ```
  /dev/Palisade> bash quickstart.cmd
  ```
* On Windows:
  ```
  C:/dev/Palisade> quickstart.cmd
  ```

This will perform the following tasks necessary to set-up and start using Palisade:
* Download each of the Palisade repos ([common](https://https://github.com/gchq/Palisade-common), [readers](https://github.com/gchq/Palisade-readers), [clients](https://github.com/gchq/Palisade-clients), [services](https://github.com/gchq/Palisade-services), [examples](https://github.com/gchq/Palisade-examples))
    - Since Palisade remains in active development, we will be pulling the 0.4.0 release which uses a REST-based microservice architecture
* Install each project in order of any dependencies
* Run the Palisade local-jvm example (more details [can be found here](https://github.com/gchq/Palisade-examples/tree/develop/deployment/local-jvm))

Once complete, you will have each of the Palisade projects cloned to your local machine.
Each Palisade module wil be installed into your `~/.m2` cache and jars built to the appropriate `.../target` directories.
The services will be running locally in separate JVM processes.

The script will have done an example run-through of Palisade, demonstrating a client with different users and purposes querying some Avro files for employee data, with some redaction and masking rules in place.
The output of this example run-through will be written to stdout once it has completed.
The logging output of all the services can be found in the `Palisade-services` directory.
More details of these rules and data structures [can be found here](https://github.com/gchq/Palisade-examples/tree/develop/example-library).

The running services can be viewed from a Eureka dashboard visible at [http://localhost:8083](http://localhost:8083), and can be shutdown with a REST POST to their `/actuator/shutdown` endpoint.
This shutdown procedure can be automated using the [quickstop.cmd script](quickstop.cmd):
* On Linux/MacOS:
  ```
  /dev/Palisade> bash quickstop.cmd
  ```
* On Windows:
  ```
  C:/dev/Palisade> quickstop.cmd
  ```
See the individual repositories and modules for their specific documentation from this point.

## Alternative Deployments

### Kubernetes
Palisade is also set-up for a kubernetes deployment, which [is documented here](https://github.com/gchq/Palisade-examples/tree/develop/deployment/local-k8s).
Under this setup, the `Palisade-services` directory will need to be rebuilt with a `mvn install` (the `quickstart.cmd` script uses a `-Pquick` profile which skips docker image builds).

## Status
Palisade is still in the early stages of development and is not production ready.
This information will be updated when there is a scheduled date for the production release.

### License
Palisade is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).
