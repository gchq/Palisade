# Hierarchical Policy Service

A policy service that uses a cache service to persist the policies and allows 
policies to be inherited. The inheritance model is such that each leaf resource 
(file or stream resource) will have a parent resource which could also have 
policies attached to it which the leaf resource would inherit. The purpose of 
this is so a data owner only has to set the policy once for a directory 
containing 100's of files rather than setting the same policy on every one of 
those files. This policy service also allows for policies to be set on the data 
type which would also get inherited by any resource of that data type.
