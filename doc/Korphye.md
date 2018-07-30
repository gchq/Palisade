# Korphye Rules

Palisade can use the [Korphye](https://github.com/gchq/koryphe/blob/master/README.md "Korphye ReadMe") library's predicate functions for implementing/applying rules and policies.
This allows commonly used Korphye function, such as `IsMoreThan`, to be applied against a record's field, such as timeStamp, without having to implement and create new lambda functions in the code.

#### Without Korphye Rules 
Each rule must be implemented in code like with the below example from [IsExampleObjRecent](https://github.com/gchq/Palisade/blob/gh-2-koryphe-integration/example/example-model/src/main/java/uk/gov/gchq/palisade/example/rule/IsExampleObjRecent.java "IsExampleObjRecent"). 

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

In the this example the threshold value is passed in at the `constructor` and the `>` logic is implemented within the `apply()` method, which is also responsible for the selecting the correct field out of the record type `ExampleObj`.
To implement the same logic, against either a different field or object, would require a new implementation of a similar class with duplicated logic.

#### Using Korphye Rules
The logic of a Korphye predicate rule is reusable only threshold value and the field selection changes.
```java
rule("2-ageOff",
    new TupleRule<>(
        select("Record.timestamp"),
        new IsMoreThan(12L)
    ))
```
The the above example from [ExampleSimpleClient](https://github.com/gchq/Palisade/blob/gh-2-koryphe-integration/example/single-jvm-example/single-jvm-example-client/src/main/java/uk/gov/gchq/palisade/example/client/ExampleSimpleClient.java "ExampleSimpleClient") shows a rule named "2-ageOff". This rule applies the `IsMoreThan` function with a threshold of 12, against the "timestamp"" field within the Record object. To apply the same rule against another field or object only the selection argument needs to be changed.

##### Nested Argument
The Korphye rules are applied against a tuple with named fields (or by default wrapped in a inefficient Reflective Tuple). This allows selecting nested fields within a record or object, i.e. `select("library.room.shelf.book.paragraph.sentence.word")`, without having to implement the logic within Java code, provided the fields or getMethods for the stuctures are public.