# Redirector

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The code in this module provides the Palisade facility for redirecting requests to Palisade Services to different live instances of that service. Principally this is intended for Data Service 
instances, but can equally be applied to any service. The redirector is a key component of auto-scaling in Palisade, allowing new instances of services to be created and terminated as demand requires
and discovered by other Palisade components.

The actual redirection logic is separated from the mechanics of redirection to allow different strategies to be used.