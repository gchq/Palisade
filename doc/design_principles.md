<!---
Copyright 2020 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->

## Design Principles
* Reduce the work required to adopt new data processing and data storage technologies.
* The service and common modules should remain technology agnostic.
* Palisade should scale horizontally and enable monitoring, with the intention being to enable auto-scaling and auto-healing of each micro-service.
* Palisade should also make it easy to debug by enabling the tracing of a request through all the micro-services that make up Palisade.
* Minimise serialisation/de-serialisation by making use of Apache Arrow where sensible.