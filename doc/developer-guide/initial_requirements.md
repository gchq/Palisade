## Initial Requirements
#### Must have
* Can use a data access policy that is local, centralised corporately, or mix of both.
* Can use attributes about users to apply policies, with that user data being local, centralised corporately, or a mix of both.
* Can use attributes about purpose for data access to apply policies, with that purpose data being local, centralised corporately, or a mix of both.
* Policies can apply a list of rules, where a rule is a function on the record/resource, user and purpose.
* Data can only be accessed via the approved mechanism.
* Request for data, and request completion status is logged for auditing centrally or locally (preferably without logging 1000s of times for each analytic's request, for example if you had 1000 mappers reading in parallel).
* Be able to write data behind Palisade in a way that minimises effort by user.
* Be a solution that works on data in motion and at rest.

#### Should have
* Be able to integrate new (storage or processing) technologies with minimal effort.
* Be able to deploy standalone(single machine); over a Hadoop cluster; or in a containerised environment (e.g Kubernetes).
* Maximise the parallelism and performance of the read/write operations.
* Allow for predicate pushdown to the data reader of the file format being accessed.
* Be able to redact/mask cells (different to redacting a column) rather than the entire record. Might just be a need to allow policy to dictate that data needs to be transformed when sent back to client.

#### Could have
* When data is written then a data lineage graph can be updated showing where the data came from.
* Data retention policies which are used to auto-delete data that has aged off or rewrite files to remove records that have been marked for purging on a given schedule (e.g. every night or once a month).