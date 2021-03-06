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

# Security considerations

During the design of Palisade, we spent some time thinking about how we would be able to authenticate who the user is (the user that is requesting access to the data).
The security boundaries for the recommended K8s deployment of Palisade are at the point of the client connecting to the Palisade, Filtered-Resource or Data Services (any service exposed by some ingress).
This can mostly be handled by the User Service authenticating the original request and credentials from the Palisade Service, with all others using the supplied token generated by the Palisade Service.

The main reason this gets tricky is because we expect Palisade to be used to access data in a distributed way over a cluster.
Therefore, the client authentication token is only going to be available on the machine that the client is running the client code on and not necessarily available at the point at which the distributed code is requesting the data from the Data Services.
That is why we have to register the data request with the Palisade Service - the Palisade Service can then provide a token to be passed to the Filtered-Resource or Data Service to authenticate that the same user is requesting access to the desired resource.
This keeps the task of authentication decoupled and handled solely by the User Service.
The Data Service validates the token and uses it to get the required and trusted details from a shared cache (see the Attribute-Masking Service).

This is to satisfy Hadoop's security architecture.
In environments where users run as themselves (as opposed to everybody running as the Hadoop processing user), we can do client-auth at on connection to the Palisade Service and again at the connection to Data Service.
The underlying mechanism still works identically, but in addition we can also validate that a user is using the Token assigned to them by the Palisade Service and detect if that Token is being used by a different user (spoofing).
This validation check can not be done with Hadoop.
