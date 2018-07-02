# Resource Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the resource service.

The resource service is the Palisade component that determines what resources are available that meet a specific
(type of) request and how they should be accessed. This interface details several methods for obtaining a list of
resources, e.g. by type or by data format. The methods of this service all return `CompletableFuture`s of
`Map`s which link a valid Palisade `Resource` with a `ConnectionDetail` object. The
`ConnectionDetail` objects contain information on how to set up a connection to retrieve a particular resource.
Implementations of this service do not deal with the filtering or application of security policy to the resources.
Therefore, a result returned from a method call on this interface doesn't guarantee that the user will be allowed to
access it by policy. Other components of the Palisade system will enforce the necessary policy controls to prevent
access to resources by users without the necessary access rights.

Implementation note: None of the getResourcesByXXX  methods in this class will return in error if there
don't happen to be any resources that do not match a request, instead they will simply return empty `Map`
instances.