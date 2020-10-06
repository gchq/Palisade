
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
# Quickstart Guide

## Getting started

### Prerequisites
Before running, make sure you have installed and appropriately-configured the following:
* [Git](https://git-scm.com/downloads)
* [OpenJDK Java 11](https://openjdk.java.net/projects/jdk/11/) or [Oracle Java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
* [Apache Maven 3](https://maven.apache.org/download.cgi)

## Running the Quickstart Script
Run the cross-platform [quickstart.cmd script](/quickstart.cmd):

```
    - On Linux/MacOS -
bash quickstart.cmd

    - or Windows -
start quickstart.cmd
```

This will perform the following tasks necessary to set-up and start using Palisade:
* Download each of the Palisade repos (common, readers, clients, services, examples)
* Install each project in order of any dependencies
* Run the Palisade local-jvm example

Once complete, you will have each of the Palisade projects cloned to your local machine.
Each Palisade module wil be installed into your `~/.m2` cache and jars built to the appropriate `.../target` directories.
The services will be running locally in separate JVM processes.

The output will have done an example run-through of Palisade, demonstrating a client with different users and purposes querying some Avro files for employee data, with some redaction and masking rules in place.

The running services can be viewed from a Eureka dashboard visible at [http://localhost:8083](http://localhost:8083), and can be shutdown with a REST POST to their `/actuator/shutdown` endpoint.
See the repo-specific documentation from this point.

## Status
Palisade is still in the early stages of development and is not production ready.
This information will be updated when there is a scheduled date for the production release.

### License
Palisade is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).
