# Config Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the config service.

There are two public methods which are `get()` and `add()`.

The purpose of the config service is to centralise the configuration of all the 
Palisade services in a single deployment. This makes it easier to support rolling 
upgrades and minimise the amount of bootstrap information each service requires 
to be bundled in the deployment jar.

The config service is designed to provide basic information to a service to allow it to initialise
itself and start functioning. One of the principle pieces of information provided by the config
service will be the locations of other Palisade services. For example, given Palisade service A
that depends on talking to service B, when service A starts up the information it receives from
the config service should tell it how to contact an instance of service B. Thus, the only information
that a service needs to have beforehand (i.e. its bootstrap information) is how to contact the
config service.

### Design

The config service has a simple interface. The `get()` API returns a `ServiceConfiguration` object
which is a map of string key/value pairs that is handed back to the service that made the request.
The only information that the config service needs on a get request is the service class making the
request. This information is contained in the `GetConfigRequest` object.

The returned `ServiceConfiguration` should contain only the information relevant and necessary to
that service. It should not contain information relating to other services in a production environment
(though this maybe acceptable for test environments) and should not contain unnecessary information
as that encourages bloat and increases the chances of an information leak between services.

One **crucial** key in the map should be the name of class to instantiate for a given service
class. For example, having retrieved the `ServiceConfiguration` for the service class `PalisadeService`
their should be a mapping from a key of `PalisadeService` to `SimplePalisadeService` (as an example) which implements
it. This is needed so the correct implementation of a service can be instantiated.

Authentication and authorisation is a separate concern; however, we note here how crucial this is
to the config service since there is likely to be sensitive information stored by it that should
not be handed out to anonymous clients.

Clients can request their configuration from the config service by making a `get` request with
an empty service field in the `GetConfigRequest`. This signifies to the config service to only
provided the minimal and safe configuration data to the requester. Nominally for a client,
this might only include the location of where to find an instance of the `PalisadeService` since
that is the client's entry point into the system.

The config service differs from other services in one crucial but necessary way: it must be able
to start and enter a running state on its own. It must contain enough bootstrap information to
initialise itself without talking to another external service. The amount of bootstrap information
should still be minimal though, i.e. to allow the config service to make itself basically functional.
It doesn't need to contain full information to allow the config service to become fully operational.
The `ConfigurationService` class contains a `configureSelfFromConfig()` method that acts as the second
stage of config service initialisation. Essentially, this allows the config service to achieve full
operational status by loading any extra information from the stored configuration. This "self-configuration"
allows the bootstrap information to kept to a minimum. An example is below:

1. Config service starts up and reads its bootstrap information.
2. Config service uses bootstrap information to connect to backend storage.
3. Config service is now able to retrieve further information it might need about itself from
backend storage.
4. Config service calls `configureSelfFromConfig()` to complete its configuration, e.g. credentials
for sensitive configuration data.
5. Config service is now fully operational.

### Operation

Each `Service` class in Palisade has two methods which should be self-explanatory in name:
`applyConfigFrom()` and `recordCurrentConfigTo()` to load and save their configuration data to and
from a `ServiceConfiguration` object respectively. The first is the more important as this is what
the `Configurator` class will call to set up instances of Palisade services with information that it
has obtained from the config service.

The general form of a Palisade service initialisation works in the following order of play:

1. Load details of config service from bootstrap information.
2. Repeatedly try to contact the config service to retrieve configuration information.
3. Call `applyConfigFrom()` to configure service.
4. Service then enters runnable state.

### Configurator

As most of the details of creating services and retrieving the configuration information from
the config service are the same across services, then this class provides convenience methods
for doing so.

The methods contained in the `Configurator` class allow for the creation of a service from a given
configuration and more importantly, creating a runnable service from a service class name and a
config service. It also provides timeout variants on all methods to allow the user to make the choice
between retrying indefinitely or failing after a given amount of time.

The `Configurator` class also allows for certain configuration keys to be overridden via Java system properties. The
methods in that class accept a list of regular expressions that specify which keys can be overridden. For example, if
service A starts up and wishes to allow configuration keys for its storage provider to be set manually from system properties,
then it calls a method in this class with an override regex of `^.*\.storage\.backend.*$`. Thus, if the `ServiceConfiguration` provided by the config
service contained the key `primary.storage.backend.class`, then as this matches the regex, a similarly named system property
would replace the value in the `ServiceConfiguration`. The main purpose of this facility to allow for external orchestration
systems to inject specific configuration items into a service's configuration without needing to interact with the config service.