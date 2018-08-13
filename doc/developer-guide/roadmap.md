## Roadmap for Palisade

#### Near term
* Create a data service broker that uses a queueing mechanism like ActiveMQ/RabbitMQ.
* Create a HDFS resource service.
* Create a MongoDB policy service.
* Create a HDFS text file data service.
* Create a HDFS CSV file data service.
* Create a HDFS Parquet data service.
* Create a HDFS Avro data service.
* Create command line tool client (so you can alias 'cat', 'grep', etc).
* Explore deployment methods (Ansible/Kubernetes).
* See if we can hook Palisade into Alluxio, so Alluxio can act as the client code.
* Create an Apache Spark client.
* Create a REST client to enable easier coded API access to data (Python/Java client).

#### Mid term
* Add support for pushing down user filters (predicate pushdown).
* Add write support to the palisade service so it updates the relevant policies, which are set ready to read data back out.
* Support Java 11+.

#### Long term
* Be able to update a data lineage graph which keeps track of what data sources were used to create this data set and what processing was done at each stage.
* Be able to manage the deletion of data (file/record/item level) to which Palisade is protecting the access, based on purge policies.
