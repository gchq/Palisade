# User Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The User service is responsible for providing the other Palisade components with knowledge of the users of the system. Note that we are avoiding having a separate notion of "Palisade"" users that are distinct from the normal user accounts of the system. There is no such thing as a "Palisade"" user.

One of the purposes of this service is to allow Palisade to adopt the whatever notion of *user* the host environment has. For example, this may from a central directory service such as LDAP, host operating system account or PKI based user authentication.

The user service separates this concern from the rest of the system. Other components use this service's API to request user details. Some deployments may also allow Palisade to add users to the system, hence the presence of the `addUser()` method in the `UserService` interface.

## API Usage

Currently the `UserService` supports adding and retrieving of user details via the two following API calls.

* `CompletableFuture<User> getUser(final GetUserRequest request) throws NoSuchUserIdException`

* `CompletableFuture<Boolean> addUser(final AddUserRequest request)`

The `addUser` method is used in the example code to allow the configuration of new users for Palisade. The request object contains the details of the user to add. The `getUser` method allows the retrieval of user details via the `User`'s ID. This is provided to Palisade when the `User` object is created.

### Notes

The API is reasonably simple at present and it is likely that this interface will grow.

Specifically, we anticipate that `UserService` implementations will connect to a account provisioning service as explained above, which will let Palisade retrieve the details of users that it previously knows nothing about. That is, we do **not** expect that every user retrieved via `getUser` will have previously been added via a corresponding `addUser` call.