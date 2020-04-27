
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



## Developer Guide


### Status
Palisade is still in the early stages of development and is not production ready.  This information will be updated when there is a scheduled date for the production release.
 
Current version of Palisade 0.4.0


###Getting Started

Prerequisites:<br/>
&nbsp;&nbsp;[Java](https://openjdk.java.net/projects/jdk/11/): Java 11 (OpenJDK 11) has been used in the development of Palisade<br/>
&nbsp;&nbsp;[Git](https://git-scm.com/) access to repositories listed below<br/>
&nbsp;&nbsp;[Maven](https://maven.apache.org/) is needed for the build process<br/>
&nbsp;&nbsp;[Docker](https://www.docker.com/) is needed for the building micro-service containers<br/>


 

####GitHub repositories:
 
  The Palisade project has been divided into a set of separate GitHub repositories for simplification of development and maintenance. 
  They consists of the following:
  
  
[Palisade](https://gchq.github.io/Palisade)
Documentation for how Palisade works at a high level

[Palisade Client](https://github.com/gchq/Palisade-clients)
Library of client code for using Palisade from different data processing technologies

[Palisade Common](https://github.com/gchq/Palisade-common)
Common code used by many of the Palisade repositories(working to remove need for this repo)

[Palisade Examples](https://github.com/gchq/Palisade-examples)
Code examples showing an example of how Palisade can be used

[Palisade Integration Tests](https://github.com/gchq/Palisade-integration-tests)
Tests for the integration of separate components

[Palisade Readers](https://github.com/gchq/Palisade-readers)
Library of code for connecting Palisade into different data storage technologies

[Palisade Services](https://github.com/gchq/Palisade-services)
Library of code for the services used in the application

 
Where Palisade Client, Common, Readers, Services are all required to run the existing solution.  The Examples provides a demonstration the automated policy rule enforcement when users read some data. They should provide a good start to understanding how Palisade works.

For an overview of the examples click [here](https://github.com/gchq/Palisade-examples).



We welcome contributions to the project. Detailed information on our ways of working can be found [here](ways_of_working.md).

The following gives some useful documentation intended to help with the developer onboarding experience.

* [Initial Requirements](initial_requirements.md) - the initial design requirements for Palisade.

* [Design Principles](design_principles.md) - the principles used throughout the design process.

* [High level architectural diagram](component_descriptions.md) - shows how the high level services link together

* [High level architecture using a map reduce client](map_reduce_architecture.md)

* [Standard flow for a read request through the Palisade system](read_process.md) - Message Sequence Chart (MSC) covering this flow

* [How might the system be deployed?](deployment_ideas.md)

* [Security considerations](security_considerations.md) - design thoughts on security

* [Roadmap for Palisade](roadmap.md) - where Palisade is heading.

* [Ways of Working](ways_of_working.md) - developer guide to branching strategy etc.



## License

Palisade is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).



## FAQ

1. What is the version of Java is supported?   The existing version of the application is built with Javas 11.  It should work with later versions of Java, but this has not been tested and cannot be verified
1. What build environments are supported?  We do not currently support Windows as a build environment, If you are running on Windows then you will need this: Microsoft Visual C++ 2010 SP1 Redistributable Package

