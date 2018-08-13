## Description of each component
1. [Client code](#client-code)
2. [Palisade Service](#palisade-service)
3. [Data Service](#data-service)
4. [Audit Service](#audit-service)
5. [User Service](#user-service)
6. [Policy Service](#policy-service)
7. [Resource Service](#resource-service)
8. [Cache Service](#cache-service)

### Client code
The job of the client code is to send the request for data into Palisade and to interpret the result as required for the data processing technology it is written for.
The responsibility for implementations of the client code is to provide users with a way to request data from Palisade in a way that the user has to make minimal changes to how they would normally use that processing technology.
Implementations of this component will usually require deep understanding of the data processing technology in order to best hook into that technology, without needing to fork the code for that technology.

### Palisade Service
The palisade service is the main controller of the Palisade system. 
[Check out the palisade service readme for more details](../../service/palisade-service/README.md)

### Data Service
The data service is responsible for connecting to the data and sanitising the data before streaming it back to the client.
[Check out the data service readme for more details](../../service/data-service/README.md)

#### Data Reader
The job of the data reader is to read a single resource, de-serialise it into the format that the set of rules for this resource will expect and then apply those rules before passing the data back to the data service. The data reader should also be able to push down filters where appropriate to optimise the read process, e.g. to only retrieve certain columns from a columnar database or a particular row range.

### Audit Service
The audit service is responsible for recording audit logs as requested by other services.
[Check out the audit service readme for more details](../../service/audit-service/README.md)

### User Service
The user service is responsible for retrieving details about the user from an authoritative source.
[Check out the user service readme for more details](../../service/user-service/README.md)

### Policy Service
The job of the policy service is to provide the set of rules (filters or transformations) that need to be applied to each resource that has been requested, based on the user and justification.
[Check out the policy service readme for more details](../../service/policy-service/README.md)

### Resource Service
The job of the resource service is to provide the detailed information about each resource based on a resource id (path/alias) being managed by Palisade. That extra information could be the type of data in that resource, when the resource was created, etc.
[Check out the resource service readme for more details](../../service/resource-service/README.md)

### Cache Service
The job of the cache service is to provide a shared cache of the information that the data service is going to require about a read request that has been registered. 
[Check out the cache service readme for more details](../../service/cache-service/README.md)