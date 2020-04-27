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
