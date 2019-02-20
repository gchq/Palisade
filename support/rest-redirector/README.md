# REST Redirection Service
A REST implementation of the redirection capability. This service can act as a redirector for another Palisade service.
It uses HTTP temporary redirects to send the client to an alternative location. 

The REST redirector needs to be launched inside a servlet container. An example of this can be found inside the [example](../../example/README.md)
directory. The REST redirector will attempt to connect to a configuration service upon construction. The details for the configuration service
should be contained in a file named by the system property "palisade.rest.config.path". The REST director will then configure itself according
to the details provided to it by the configuration service. Some of the configuration items can be overridden by setting extra system properties.

| System property | Description |
|-----------------|-------------|
| rest.redirect.redirector | JSON serialised class that implements the redirection business logic. Must implement `uk.gov.gchq.palisade.redirect.Redirector`. |
| rest.redirect.class | Class type for the Palisade service being redirected. Must extend `uk.gov.gchq.palisade.service.Service`. |
| rest.redirect.rest_impl.class | Class type that implements the REST end point for this service type.  |

Please note this REST implementation does not include any authentication.
