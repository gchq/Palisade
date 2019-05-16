# REST Redirection Service
A REST implementation of the redirection capability. This service can act as a redirector for another Palisade service.
It uses HTTP temporary redirects to send the client to an alternative location. 

The REST redirector will attempt to connect to a configuration service upon construction. The details for the configuration service
should be contained in a file named by the environment variable "PALISADE_REST_CONFIG_PATH". The REST director will then configure itself according
to the details provided to it by the configuration service. Some of the configuration items can be overridden by setting extra system properties.

| System property | Description |  Example |
|-----------------|-------------|----------|
| rest.redirect.redirector | JSON serialised class that implements the redirection business logic. Must implement `uk.gov.gchq.palisade.redirect.Redirector`. | <...some serialised JSON...> |
| rest.redirect.class | Class type for the Palisade service being redirected. Must extend `uk.gov.gchq.palisade.service.Service`. | `uk.gov.gchq.palisade.data.service.impl.SimpleDataService` |
| rest.redirect.rest_impl.class | Class type that implements the REST end point for this service type.  | `uk.gov.gchq.palisade.data.service.impl.RestDataServiceV1` |

The REST redirector should be launched inside of a servlet container. An example of this can be found in the [Local JVM](../../example/deployment/local-jvm/README.md) example. Under the `example/example-services/example-rest-redirector-service`
directory. There a `Launcher` class starts a servlet container with a RESTRedirector inside it. The RESTRedirector servlet should be configured with the same URL path as the service
it is redirecting.

Please note this REST implementation does not include any authentication.
