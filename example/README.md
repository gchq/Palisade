# Examples

The Example module contains all the example specific modules:

- hr-data-generator, which can create a fake sample dataset based around possible data that a company might hold about its employees.
- example-model, contains all the deployment agnostic example code such as the rules and example configuration.
- example-services, contains all the example JVM REST based services
- deployment, contains all the deployment specific code and scripts



The example demonstrates different users querying an avro file over a REST api.

The queried file is a generated HR dataset of Employee records. Here is the format of the Employee class:  
    private UserId uid;  
    private String name;  
    private String dateOfBirth;  
    private PhoneNumber[] contactNumbers;  
    private EmergencyContact[] emergencyContacts;  
    private Address address;  
    private BankDetails bankDetails;  
    private String taxCode;  
    private Nationality nationality;  
    private Manager[] manager;  

   The manager field is an array of manager, which could potentially be nested several layers deep, in the generated example manager is nested 3 deep.


The policies and users have been hardcoded in class: uk.gov.gchq.palisade.example.client.ExampleSimpleClient.

Policy have defined the following rules:

1. BankDetailsRule - The bankDetails field should be returned if the user querying the file has the PAYROLL role and the purpose of the query is SALARY  
   In all other cases the bankDetails field should be redacted.

1. DutyOfCareRule - This rule is concerned with the contactNumbers and emergencyContacts fields. These fields should be returned 
    - if the user querying the file has the HR role and the purpose of the query is DUTY_OF_CARE
    - if the user querying the file is the line manager of the Employee record being queried and the purpose of the query is DUTY_OF_CARE  
   In all other cases these fields should be redacted.

1. NationalityRule - The natonality field should be returned if the user querying the file has the HR role and the purpose of the query is STAFF_REPORT    
   In all other cases the nationality field should be redacted.

1. ZipCodeMaskingRule - This rule is concerned with the address field.
    - if the user querying the file has the HR role then the whole address is returned
    - if the purpose of the query is DUTY_OF_CARE and the user querying the file is the line manager of the Employee record being queried then the whole address is returned
    - if the user querying the file has the ESTATES role then the address field should be returned with the zipcode/postcode masked to reduce its precision  
   In all other cases the address field should be redacted.
  
The ExampleConfigurator class creates the users and uses the rule classes mentioned above to create the rules. The example will be run with 3 users:

   - Alice has the roles HR and PAYROLL
   - Bob has the role ESTATES
   - Eve has the role IT

The example runs several different queries by the different users, with different purposes. When you run the example you will see the data has been redacted in line with the rules.

For deployment specific instructions on how to run the example see:  
[Local JVM](deployment/local-jvm/README.md)  
[Local Docker](deployment/local-docker/README.md)  
[Local Kubernetes](deployment/local-k8s/README.md)  
