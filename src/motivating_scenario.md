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

# Motivating Scenario

To give some grounding to an otherwise abstract problem, we refer to an example scenario.
This scenario involves a number of different users requesting access to some sensitive Human Resources (HR) data for different reasons.
These different requests each demonstrate a different aspect of Palisade that we require.

> :information_source:
This scenario is implemented and demonstrated in the [Example Library](https://github.com/gchq/Palisade-examples/tree/develop/example-library) package of the examples repository.


### Employee

The `Employee` represents an element of the target dataset, the public and private details of an employee at a company.
Some highlights of the schema might include:
```
class Employee:
    String uid
    String name
    Address address
    BankDetails bankDetails
    PayGrade payGrade
    List<Manager> managers

class Manager:
    String uid
    List<Manager> managers
```


### Users

The `ExampleUser` represents a user of Palisade, an employee who needs to analyse the sensitive data of other fellow employees.
Some highlights of the schema might include:
```
class User:
    String uid
    List<String> auths

class ExampleUser extends User:
    List<TrainingCourse> trainingCompleted

enum TrainingCourse:
    PAYROLL_TRAINING_COURSE
```
In particular, `TrainingCourse` is specific to this scenario and not to Palisade, it has been added as part of this enriched `ExampleUser`.


### Resources

The `Resource` represents a coarse-grained collection of records with a hierarchical structure, although it does not actually contain the data therein.
A `Resource` in this scenario is effectively a filename, from the following directory structure:
```
/data
  employee_file0.avro
  employee_file1.avro
```
These are provided by a _data catalogue_ which is distinct from the target _data source_, and can be tagged with additional metadata.


### Purposes

The `Purpose` of the data request is declared by the `User` along with the rest of the query.
This is audited, and could have further rules deciding whether the declared `Purpose` was legitimate.
A sample of the set of possible values that could be declared in our scenario might include:
```
enum Purpose:
    SALARY
    DUTY_OF_CARE
    STAFF_REPORT
```
In general, we bundle all additional contextual information into the `Context` of the request.


### Policies

With these, we hope to demonstrate Palisade is capable of applying complex record-level rules as part of the defined data-access policy, such as:
* resource-level filtering - hide a resource `File /data/employee_file0.avro`
* record-level filtering - hide the whole `Employee Alice` record
* record-level masking - show the first half of `Alice`'s `Address`'s `PostCode` and hide the rest of the address
* contextual rule application - show the full `BankDetails 1234-5678-90AB-CDEF` if the purpose of the request was `SALARY` and the user has completed the `PAYROLL_TRAINING_COURSE`, otherwise hide it
* complex (recursive) rules - show `Alice` the `PayGrade` of employees for whom she is in their management chain (managers of managers etc.)
