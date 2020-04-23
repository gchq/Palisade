
# <img src="logos/logo.svg" width="180">
### Scalable Data Access Policy Management and Enforcement


##Development


### Status
Palisade is still in the early stages of development and is not production ready.  This information will be updated when there is a scheduled date for the production release.
 
Current version of Palisade 0.1


###Getting Started

Prerequisites:<br/>
&nbsp;&nbsp;[Java](https://openjdk.java.net/projects/jdk/11/): Java 11 (OpenJDK 11) has been used in the development of Palisade<br/>
&nbsp;&nbsp;[Git](https://git-scm.com/) access to repositories listed below<br/>
&nbsp;&nbsp;[Maven](https://maven.apache.org/) is needed for the build process<br/>
&nbsp;&nbsp;[Docker](https://www.docker.com/) is needed for the building micro-service containers<br/>


  
 <span style="color:red">
 Note: We do not currently support Windows as a build environment, If you are running on Windows then you will need this: Microsoft Visual C++ 2010 SP1 Redistributable Package
 </span>

 

####GitHub repositories:
 
  The Palisade project has been divided into a set of separate GitHub repositories for simplification of development and maintenance. 
  They consists of the following:
  
  
[Palisade](https://gchq.github.io/Palisade)
Documentation for Palisade

[Palisade Client](https://github.com/gchq/Palisade-clients)
Code for initiating Client data requests.

[Palisade Common](https://github.com/gchq/Palisade-common)
Common code used by all of the Palisade repositories

[Palisade Examples](https://github.com/gchq/Palisade-examples)
Code examples showing how Palisade can be used

[Palisade Integration Tests](https://github.com/gchq/Palisade-integration-tests)
Tests for the integration of separate components

[Palisade Readers](https://github.com/gchq/Palisade-readers)
Code for performing data queries on behalf of a client

[Palisade Services](https://github.com/gchq/Palisade-services)
Code for the services used in the application

 
Where Palisade Client, Common, Readers, Services are all required to run the existing solution.  The Examples provides a demonstration the automated policy rule enforcement when users read some data. They should provide a good start to understanding how Palisade works.

For an overview of the examples click [here](https://github.com/gchq/Palisade-examples).

For more details about the code structure see the [Developer Guide](doc/developer-guide/developer_guide.md)

For details about possible types of clients and how they might function, click [here](doc/possible_clients.md)


## License

Palisade is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).


## Contributing
We welcome contributions to the project. Detailed information on our ways of working can be found [here](https://gchq.github.io/Palisade/doc/other/ways_of_working.html).

?? Do we want a public email address and if so what will it be?
For any questions or help please contact information@palisade.com

## FAQ

1. What is the version of Java is supported?   The existing version of the application is built with Javas 11.  It should work with later versions of Java, but this has not been tested and cannot be verified
1. What build environments are supported?  We do not currently support Windows as a build environment, If you are running on Windows then you will need this: Microsoft Visual C++ 2010 SP1 Redistributable Package
