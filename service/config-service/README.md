# Config Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the config service.

There are two public methods which are `get()` and `add()`.

The purpose of the config service is to centralise the configuration of all the 
Palisade services in a single deployment. This makes it easier to support rolling 
upgrades and minimise the amount of bootstrap information each service requires 
to be bundled in the deployment jar.