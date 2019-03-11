# Simple Palisade Service

A simple implementation of a Palisade Service that just connects up the
Audit, Cache, User, Policy and Resource services.

It currently doesn't validate that the user is actually requesting the correct
resources. It should check the resources requested in getDataRequestConfig
are the same or a subset of the resources passed in in registerDataRequest. 

This implementation of `PalisadeService` also implements `PalisadeMetricProvider`
which provides metrics on what is happening within the Palisade system.
