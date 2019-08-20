# Client Implementations

The job of the client code is to send the request for data into Palisade and to interpret the result as required for the data processing technology it is written for.
The responsibility for implementations of the client code is to provide users with a way to request data from Palisade in a way that the user has to make minimal changes to how they would normally use that processing technology.
Implementations of this component will usually require deep understanding of the data processing technology in order to best hook into that technology, without needing to fork the code for that technology.


This directory contains the various client implementations for Palisade that have currently been written. Some are intended to be used as standalone implementations such
as the [cat client](cat-client/README.md), whilst others are the necessary client library implementations to allow Palisade to be
used with other frameworks such as the [MapReduce client](mapreduce-client/README.md).
