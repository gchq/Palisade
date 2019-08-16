#Stroom Audit Service

##Requirements
All audit logs should contain the following information:
 - System name and description making the request
 - Time/Date of the audit log creation
 - System hostname and IP making the audit log request
 - A session ID to link all audit logs created from one data access request

Stroom audit service needs to log at the following points
 1. Log if the authentication was not successful
    - log the user ID
    - log the context information
    - log the resource ID
 1. Log if the resource service cannot resolve the resource id
    - log the user ID
    - log the context information
    - log the resource ID
 1. Log if any other part of the registration stage failed
    - log the user ID
    - log the context information
    - log the resource ID
 1. Log what resources the user has been approved to access
    - log the list of resources
    - log the user id
    - log the context
 1. Log if the user has registered the request before requesting a resource from a data service
    - log the registration token
    - log the resource requested
    - log the error message
 1. Log if any errors occur when processing a request for data
     - log the registration token
     - log the resource requested
     - log the error message
 1. Log when the request for data has been completed
     - log the resource
     - log the number of records returned
     - log the number of records processed
     - log the human readable explanation of the rules being applied
     - log the user id
     - log the context
