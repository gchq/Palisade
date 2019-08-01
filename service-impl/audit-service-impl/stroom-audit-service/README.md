#Stroom Audit Service

##Requirements
All audit logs should contain the following information:
 - System name and description making the request
 - Time/Date of the audit log creation
 - System hostname and IP making the audit log request
 - A session ID to link all audit logs created from one data access request

Stroom audit service needs to log at the following points
 1. Log the initial request when a user registers a data access request
     - log the user ID
     - log the context information
     - log the resource ID
 2. Log whether the authentication was successful or not
 3. Log if any other part of the registration stage failed
 4. Log what resources the user has been approved to access
 5. Log when the user requests a resource from a data service
     - log the resource requested
 6. Log if any errors occur when processing a request for data
     - log the registration token
     - log the resource requested
     - log the error message
 7. Log when the stream of data back to the client is started
     - log the resource
     - log the human readable explanation of the rules being applied
 8. Log when the request for data has been completed
     - log the resource
     - log the number of records returned
     - log the number of records processed
