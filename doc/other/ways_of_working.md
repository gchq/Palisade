# Ways of Working

## Contents
1. [Git branching model](#git-branching-model)
2. [Issues](#issues)
3. [Pull requests](#pull-requests)
4. [Coding style](#coding-style)
5. [Releases](#releases)

## Git branching model
We have adopted the following branching strategy: [Git Branching Model](https://nvie.com/files/Git-branching-model.pdf)

## Issues 
Where possible a pull request should correlate to a single GitHub issue. An issue should relate to a single functional or non-functional change - changes to alter/improve other pieces of functionality should be addressed in a separate issue in order to keep reviews atomic.
The reasoning behind code changes should be documented in the GitHub issue. 
All resolved issues should be included in the next GitHub milestone, this enables releases to be linked to the included issues.
If a code change requires users to make changes in order for them to adopt it then the issue should be labelled 'migration-required' and a comment should be added similar to:

```
### Migration Steps

[Description of what needs to be done to adopt the code change with examples]
```

#### Workflow
* Assign yourself to the issue
* Create a new branch off develop using pattern: gh-[issue number]-[issue-title]
* Commit your changes prefixing your commit title with: gh-[issue-number] - [commit title]
* Check your changes
* Create a pull request to merge your branch into develop (and assign label in-review to your issue)
* The pull request will be reviewed and following any changes and approval your branch will be merged into develop
* Delete the branch
* Close the issue - add a comment saying it has been merged into develop

## Pull Requests
Pull requests will undergo an in depth review by a Palisade committer to check the code changes are compliant with our coding style. This is a community so please be respectful of other members - offer encouragement, support and suggestions. 

As described in our git branching model - please raise pull requests to merge you changes in our **develop** branch.

Please agree to the [GCHQ OSS Contributor License Agreement](https://github.com/GovernmentCommunicationsHeadquarters/Gaffer/wiki/GCHQ-OSS-Contributor-License-Agreement-V1.0) before submitting a pull request.

## Coding style
Please ensure your coding style is consistent with rest of the project and follow coding standards and best practices.

In particular please ensure you have adhered to the following:
* Strive for small easy to read and understand classes and methods.
* Separate out related classes into packages and avoid highly coupled classes and modules.
* Checkstyle and findbugs are run as part of 'mvn package' so you should ensure your code is compliant with these. The project will not build if there are issues which cause these plugins to fail.
* Classes and methods should comply with the single responsibility principal.
* Avoid magic numbers and strings literals.
* Avoid duplicating code, if necessary refactor the section of code and split it out into a reusable class.
* Look after you streams - if you open one make sure you close it too. This should apply to all volatile resource usage.
* Don't swallow exceptions - ensure they are logged or rethrown.
* Give credit for other peoples work.
* Update the NOTICES file for changes to the dependencies.
* Consider the scope of dependencies - restrict them when possible using the appropriate maven scope.
* Don't expose private logic in classes through public methods.
* Try to avoid static classes/methods.
* Field access should be controlled via getters and setters.
* Make use of the core Java API - don't reinvent the wheel.
* Make use of generic typing.
* Make use of appropriate object oriented design patterns.
* Use Loggers instead of System.out.print and throwable.printStackTrace.
* Ensure that the toString(), equals() and hashCode() methods are implemented where appropriate.

#### Javadoc
Ensure your java code has sufficient javadocs explaining what the section of code does and the intended use of it. Javadocs should be used in addition to clean readable code, it is not an excuse to write lazy code.

In particular:
* All public classes (not required for test classes unless an explanation of the testing is required)
* public methods (not required if the functionality is obvious from the method name)
* public constants (not required if the constant is obvious from the name)

#### Tests
* All new code should be unit tested. Where this is not possible the code should be invoked and the functionality should be tested in an integration test. In a small number of cases this will not be possible - instead steps to verify the code should be thoroughly documented.
* Tests should cover edge cases and exception cases as well as normal expected behavior.
* Keep each test decoupled and don't rely on tests running in a given order - don't save state between tests.
* Overall for a given code change aim to improve the code coverage.
* Unit test classes should test a single class and be named [testClass]Test.
* Integration test classes should be named [functionalityUnderTest]IT.
* Tests should be readable and self documenting where possible. 
* Each test should focus on testing one small piece of functionality invoked from a single method call. 
* Unit tests should use JUnit 4.x.
* We suggest the following pattern:

  ```java
  @Test
  public void should[DoSomething|ReturnSomething] {
      // Given
      [Setup your test here]

      // When
      [Invoke the test method]

      // Then
      [assert the method did what was expected]
  }
  ```

## Releases
* All issues included in the release should be marked with the relevant milestone
* When the `develop` branch is ready to be released create a pull request to merge `develop` into `master`
* Merge the pull request
* Travis CI will carry out the release in 2 stages
  * Initially it will see the pom version is a SNAPSHOT, this will trigger it to tag the 
 release based on the SNAPSHOT version or you can define the version by setting the environment variable RELEASE_VERSION in Travis CI settings. 
 After tagging the release it will update the Javadoc and generate the release notes.
 It will then update the `develop` branch and update the pom version to the next SNAPSHOT version.
  * Travis CI is then automatically triggered on `master` for a second time, now with a pom version that does not contain a SNAPSHOT. 
  It will now build the binaries and release them to Nexus.
* Once Travis CI has finished both stages:
  * Log into Nexus
  * Go to the staging environment
  * Select the artifacts
  * Click 'close'
  * Click 'release' - this will release them to Maven Central.
