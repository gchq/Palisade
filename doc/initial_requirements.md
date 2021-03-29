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

# Initial Requirements

## Must have
* Can use a data access policy that is local, centralised corporately, or mix of both.
* Can use attributes about users to apply policies, with that user data being local, centralised corporately, or a mix of both.
* Can use attributes about the query to apply policies.
These attributes can be defined by the user, client or palisade service.
Examples of attributes include: purpose for the query;
system the data is being accessed from;
geo-location of where the user is accessing the data from;
or time of day the query is being run;
or a combination of these and others you might want to define.
* Policies can apply a list of rules, where a rule is a function on the record/resource, user and context.
* Data can only be accessed via the approved mechanism.
* Request for data, and request completion status is logged for auditing centrally or locally
(preferably without logging 1000s of times for each analytic's request, for example if you had 1000 mappers reading in parallel).
* Be able to write data behind Palisade in a way that minimises effort by user.
* Be a solution that works on data in motion and at rest.

## Should have
* Be able to integrate new (storage or processing) technologies with minimal effort.
* Be able to deploy standalone(single machine); over a Hadoop cluster; or in a containerised environment (e.g Kubernetes).
* Maximise the parallelism and performance of the read/write operations.
* Allow for predicate pushdown to the data reader of the file format being accessed.
* Be able to redact/mask cells (different to redacting a column) rather than the entire record.
Might just be a need to allow policy to dictate that data needs to be transformed when sent back to client.

## Could have
* When data is written then a data lineage graph can be updated showing where the data came from.
* Data retention policies which are used to auto-delete data that has aged off or rewrite files to remove records that have been marked for purging on a given schedule (e.g. every night or once a month).
