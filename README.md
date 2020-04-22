
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

## Palisade 
Palisade aims to provide a stable framework for defining and enforcing policies for accessing data.

##
####Introduction
Modern organisations can have multiple datasets that are each managed and accessed according to different policies and procedures. This can make it complicated for analysis while still ensuring reglatory compliance.  Most technologies "out of the box" do not provide the necessary solution for managing these complex issues for data access. This leads to data being siloed and often accesses being restricted with the outcome that the insight from data can be lost.   

Palisade can provide a solution to this problem.  It consists of a set of services for managing and enforcing complex data access policies across different datasets and platforms.  These services are able to provide modular data access controllers and independent peripheral services to allow policies to be integral to the process a client uses to access data.  

Consider the example of a set of datasets may have been assembled for specific purposes.   For a given dataset, only particular types of query are permitted for certain types of user.  Also, within each dataset a specific user may only be permitted to access a particular subset of the records. Palisade allows the policies for accessing datasets, redacting records and executing the query to be specified independently of the physical data and the platform hosting the data.  This will enable the clients to perform queries across the datasets while still conforming to company and regulatory policies.  

Policies can be applied on a per field basis. This means that fields (e.g. cells) in a record can be redacted or masked based on: the contents of the record; the user accessing the record; and the contextual information given by the user or the system at the time of the query.

When a user submits a query (or executes some analytic code), Palisade uses the information about the user, the platform and the data requested, to look up the relevant data access policy. This policy is then used by a process, which ideally should be a data-local process to get the best performance, to return only the records that are allowed by the policy for the specific user, dataset and query time context. 


###Additional Information 



For more technical information on how you can use Palisade, go to [Documentation](https://github.com/gchq/Palisade/blob/doc/Documentation.md)

Information on how to contribute or report issues, please go to [Contributing](https://gchq.github.io/Palisade/blob/doc/Contributing.md)


### License
Palisade is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).

 


For the latest's release of this README can be found [here](https://gchq.github.io/Palisade/blob/develop/Readme.md).




