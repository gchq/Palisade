# Koryphe Rules

Palisade can use the [Koryphe](https://github.com/gchq/koryphe/blob/master/README.md "koryphe ReadMe") library's predicate functions for implementing/applying rules and policies.
This allows commonly used Koryphe function, such as `IsMoreThan`, to be applied against a record's field, such as timeStamp, without having to implement and create new lambda functions in the code.

### Without Koryphe Rules 
Each rule must be implemented in code like with the below example from [IsExampleObjRecent](https://github.com/gchq/Palisade/blob/gh-2-Koryphe-integration/example/example-model/src/main/java/uk/gov/gchq/palisade/example/rule/IsExampleObjRecent.java "IsExampleObjRecent"). 

```java
public class IsExampleObjRecent implements Rule<ExampleObj> {
    private long threshold;

    public IsExampleObjRecent() {
    }

    public IsExampleObjRecent(final long threshold) {
        this.threshold = threshold;
    }

    @Override
    public ExampleObj apply(final ExampleObj record, final User user, final Justification justification) {
        if (null == record) {
            return null;
        }

        final boolean isRecent = record.getTimestamp() > threshold;
        return isRecent ? record : null;
    }
}
```

In the this example the threshold value is passed in at the `constructor` and the `>` logic is implemented within the `apply()` method, but this method is also responsible for getting the correct field from the record `ExampleObj.getTimestamp()`.
To implement the same logic against either a different field or object, would require a new implementation of a similar class with duplicated logic.

### Using Koryphe Rules
The logic of a Koryphe predicate rule is reusable, because only threshold value and the field selection/projections changes.

#### Selection
The below example from [ExampleSimpleClient](https://github.com/gchq/Palisade/blob/gh-2-koryphe-integration/example/single-jvm-example/single-jvm-example-client/src/main/java/uk/gov/gchq/palisade/example/client/ExampleSimpleClient.java "ExampleSimpleClient") shows a rule named "2-ageOff". This rule applies the `IsMoreThan` function with a threshold of 12, against the "timestamp" field within a Record object. To apply the same rule against another field or object only the selection argument needs to be changed.
```java
rule("2-ageOff",
    new TupleRule<>(
        select("Record.timestamp"),
        new IsMoreThan(12L)
    ))
```


##### Nested Selection
The Koryphe rules are applied against a tuple with named fields (or by default wrapped in a inefficient Reflective Tuple). This allows selecting nested fields within a record or object, i.e. `select("library.room.shelf.book.paragraph.sentence.word")`, without having to implement the logic within Java code, provided the fields or getMethods for the structures are public.

#### Projection
The below example is how Koryphe rules can be used to project a value into the record. 
```java
.rule(
    "1-replacement",
    new TupleRule<>(
        select("Record.value1"),    
        new SetValue("new value")),
        project("Record.value1")
    )
```
* A **selection** is made "Record.value1", in this example the selection isn't used)
* A **predicate** is applied, in this example the there is no logic other that a value is created "new value"
* Output is written to the **projection**, in this example the projection is the same as the original selection "Record.value1"

#### If Logic
The below example from [ExampleSimpleClient](https://github.com/gchq/Palisade/blob/gh-2-koryphe-integration/example/single-jvm-example/single-jvm-example-client/src/main/java/uk/gov/gchq/palisade/example/client/ExampleSimpleClient.java "ExampleSimpleClient") shows how decision logic can be applied using Koryphe rules. 
```java
.rule(
    "3-redactProperty",
    new TupleRule<>(
        select("User.roles", "Record.property"),
        new If<>()
            .predicate(0, new Not<>(new CollectionContains("admin")))
            .then(1, new SetValue("redacted"))
    )
```
The selection has two values `select("User.roles", "Record.property")` these can be referenced within the `If` function by their respective keys (0,1).
In the example the predicate is applied against the '0' selection "User.roles", when evaluated as true the function within the ".then" method is executed otherwise the function in the ".otherwise" function (not shown in the example.) This examples shows the value "redacted" being written to the selection '1'/"Record.property".