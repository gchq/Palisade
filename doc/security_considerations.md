## Security considerations
During the architecture of Palisade we spent some time thinking about how we would be able to authenticate who the user is that is requesting access to the data.
The security boundaries for Palisade are at the point of contacting each of the different services when deployed as micro services (which is the recommended deployment model).
For most services we would only need to authenticate that the processing user that all the services would be running as are the only ones using the public API's.
This can be done by packaging a secure key/token/certificate with each of those trusted services and passing that to the other service when making requests, for that service to check that it matches what the service is expecting.

Where this gets trickier is when the user is authenticating with one of the palisade services or one of the data service. The main reason this gets tricky is because we expect Palisade to be used to access data in a distributed way over a cluster.
Therefore the client authentication token is only going to be available on the machine that the client is running the client code on and not necessarily available at the point at which the distributed code is requesting the data from the data services.
That is why we have to register the data request with the palisade service first in a non-distributed way where you can provide a personal token/certificate to be validated to ensure authentication.
Then the palisade service can then provide a token to be passed to the data service to authenticate that the same already authenticated user is requesting access to the desired resource.
The data service validates the token with a palisade service and uses it to get the required trusted details from a cache service that is shared by all instances of the palisade service.

This is to satisfy Hadoop's security architecture. In environments where users run as themselves (as opposed to everybody running as the Hadoop processing user), we can do client-auth at on connection to the PalisadeService and again at the connection to DataService.
The underlying mechanism still works identically, but in addition we can also validate that a user is using the Token assigned to them by the PalisadeService and detect if that Token is being used by a different user (spoofing).  This validation check can not be done with Hadoop.

