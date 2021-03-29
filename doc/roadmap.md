<!---
Copyright 2018-2021 Crown Copyright

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

## Roadmap for Palisade

#### Near term
* Validate that Palisade can work at scale in a performant way.
* Validate that Palisade can work on both streaming and batch technologies in the same deployment.

#### Mid term
* Add support for pushing down user filters (predicate pushdown).

#### Long term
* Add write support to the palisade service so it updates the relevant policies, which are set ready to read data back out.
* Be able to update a data lineage graph which keeps track of what data sources were used to create this data set and what processing was done at each stage.
* Be able to manage the deletion of data (file/record/item level) to which Palisade is protecting the access, based on purge policies.
