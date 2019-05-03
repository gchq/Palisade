package uk.gov.gchq.palisade.example.common;

import uk.gov.gchq.palisade.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ExampleUser extends User {

    private Set<TrainingCourse> trainingCompleted = new HashSet<>();

    public ExampleUser() {
    }

    public ExampleUser trainingCompleted(TrainingCourse... trainingCompleted) {
        requireNonNull(trainingCompleted, "cannot add null training completed");
        Collections.addAll(this.trainingCompleted, trainingCompleted);
        return this;
    }

    public Set<TrainingCourse> getTrainingCompleted() {
        // trainingCompleted cannot be null
        return trainingCompleted;
    }

    public void setTrainingCompleted(TrainingCourse... trainingCompleted) {
        trainingCompleted(trainingCompleted);
    }
}
