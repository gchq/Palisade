## Standard flow for a read request through the Palisade system

1. The user makes use of a plug-in (client code) for the data processing technology that they are using which makes it almost invisible to them that they are actually querying data via Palisade.
2. For a distributed workflow that client code will usually be made up of a driver component and an executor/mapper component. The driver component starts by registering the data request with the palisade service, stating the resources/alias mapping to resources that they want to query, along with a user id and a justification for querying the data.
3. The palisade service receives that request and then sends an audit log message to the audit service to log the request.
4. The palisade service then requests the mapping of how to connect to the relevant data service to retrieve each of the resources requested. This may allow aliases which map to a list of resources, which would need to be resolved by the resource service.
5. The palisade service then returns the mapping to the client in response to the client registering the request
6. The palisade service then requests the full details about the user from the user service.
7. The palisade service then requests the policy to be applied to each of the resources, passing in the full user details returned by the user service, any justification passed in by the client code and the list of resources returned by the resource service.
8. The palisade service then caches the response from the policy service in the cache service, so that any palisade service can respond to the data server later on.
9. The client code, having received the mapping of resource-to-connection details, can now split the resources up over its executors and then each executor can request a subset of the resources from the relevant data service.
10. The data service receives the request to read a subset of the resources and passes that request to the palisade service.
11. The palisade service responds to the data service by retrieving from the cache service the response that was cached in step 8.
12. The data service uses the response to loop over all the resources to read the resource and apply the rules applicable to that resource.
13. The filtered and transformed data is then streamed back to the client.
14. The client then formats the code in the way that the data processing technology expects the data, so the user's code can proceed as if palisade was never there.