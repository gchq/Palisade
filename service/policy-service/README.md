# Policy Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the policy service.

The responsibilities of the policy service is to provide the set of rules
(filters or transformations) that need to be applied to each resource that
has been requested, based the user and justification.

**Note:** A resource could be a file, stream, directory or even the system
resource (policies added to the system resource would be applied globally).