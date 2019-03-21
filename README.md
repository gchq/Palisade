
<!---
Copyright 2018 Crown Copyright

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

#### Scalable Data Policy Management and Enforcement

## Status
<span style="color:red">
Palisade is still in the early stages of development.
At the moment there are just a few simple implementations to show how Palisade could work - it is not production ready.
The descriptions of Palisade below indicate the high level goals of the framework. 
</span>

## Documentation

The documentation for the latest release can be found [here](https://gchq.github.io/Palisade).


### Complex data policies

Modern data organisations store lots of datasets that are each managed and accessed according to different data policies. These policies can be complicated and it can be difficult to store and access datasets for analysis across different technologies while ensuring compliance.

Most technologies don't provide the necessary hooks out of the box for managing these complex data policies. This leads to data being siloed or accesses and APIs being restricted and insight from data is lost. 

Palisade is a set of services for managing and enforcing complex data policies across different scales and platforms. It provides modular data access controllers and independent, peripheral services to allow policies to be applied before clients access the data.  

For example, datasets may have been assembled for specific purposes and, for a given dataset, only particular types of query are permitted for certain types of user. Also, within each dataset a specific user may only be permitted to access a particular subset of the records. Palisade allows the policy for accessing datasets, redacting records and executing the query to be specified independently of the physical data and the platform hosting the data.

Policies can be applied on a per item basis. This means that items (e.g. cells) in a record can be redacted or masked based on the contents of the record, the user accessing the record and the contextual information given at query time by the user or system.

When a user submits a query (or executes some analytic code), Palisade uses the local information about the user, the platform and the data requested, to look up the relevant data policy. This policy is then used by a process, which ideally should be a data-local process to get the best performance, to return only the records that are allowed by the policy for the specific user, dataset and query time context. 

### Multi-platform compatibility

Palisade aims to provide a stable framework for defining and enforcing policy and auditing requirements for data access requests while allowing you to keep up with the fast-changing world of data science tools. Palisade does that by sharing the burden of creating connectors to new data storage technologies and the connectors from new data processing technologies into Palisade. Palisade is not dependent on any platform ecosystem such as Hadoop, Kubernetes, AWS, Azure, Google.io, etc. 

### Centralised or Local Policy

If required, Palisade allows an organisation to use centralised services which each deployment of Palisade's data access services can share. This means that your data access policies, audit logs, user account details etc. don't need to be duplicated on every system. Of course, Palisade can be deployed locally on a per platform basis.



![Palisade Overview](doc/img/Palisade_overview.jpg)


### Prerequisites
1. git
1. maven
1. to run any of the examples: docker

## Getting started

To get started, clone the Palisade repo: 

`git clone https://github.com/gchq/Palisade.git`

We have an example that demonstrate the automated policy rule enforcement when users read some data.
These example should provide a good start to understanding how Palisade works.

1. A local JVM Rest based example
1. A docker based example that will run on the local machine
1. An AWS EMR based example (coming soon)
1. An Azure HDInsight example (coming soon)

For an overview of the example see [here](example/README.md)

* To run the local JVM REST based example follow this guide: [Local JVM](example/deployment/local-jvm/README.md)

* To run the local docker REST based example follow this guide: [Local Docker](example/deployment/local-docker/README.md)

For more details about the code structure see the [Developer Guide](doc/developer-guide/developer_guide.md)

For details about possible use cases, see the [Use Cases](doc/use_cases.md)


## License

Palisade is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).


## Contributing
We welcome contributions to the project. Detailed information on our ways of working can be found [here](https://gchq.github.io/Palisade/doc/other/ways_of_working.html).


## FAQ

* What versions of Java are supported? Java 8 with plans to upgrade to Java 11+ soon.

