# Simple Config Service

A simple implementation of a Config Service that provides the basic functionality 
to get and put configuration for all the services.

## Implementation notes

The `SimpleConfigService` is designed to operate as a layer above the cache service and uses the
cache service as its backend storage provider. It stores `ServiceConfiguration` objects in its
own namespace in the cache and uses the service's class name as the key.

When the configuration information for a specific service class is requested, the `SimpleConfigService`
will make two attempts to find the configuration. Firstly, it will attempt to lookup the configuration
for that specific service class. If that fails, then a second attempt is made to find configuration
information for all services by looking up configuration for the generic `Service` class. If this
fails then an exception is thrown. Note that putting all configuration information for all services
together under a `Service` key should only be used in test deployments. Production deployments
should separate the the configuration information for each deployed service.