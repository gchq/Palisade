/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.example.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;

import java.util.ArrayList;
import java.util.EnumSet;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(value = {"trainingCompleted"})
public class ExampleUser extends User {

    public static final String TRAINING_KEY = "training completed";

    public ExampleUser(final User user) {
        setUserId(user.getUserId());
        setUserFields(user.getUserFields());
    }

    public ExampleUser() {
    }

    public ExampleUser trainingCompleted(final TrainingCourse... trainingCompleted) {
        requireNonNull(trainingCompleted, "cannot add null training completed");
        ArrayList<TrainingCourse> trainingComplete = (ArrayList<TrainingCourse>) getUserField(TRAINING_KEY);
        if (trainingComplete == null) {
            trainingComplete = new ArrayList<>();
        } else {
            trainingComplete.clear();
        }
        for (TrainingCourse training : trainingCompleted) {
            trainingComplete.add(training);
        }

        System.out.println("Nigel inserted at:");
        System.out.println(trainingComplete);

        for (TrainingCourse training : trainingComplete) {
            System.out.println(training);
        }

        System.out.println("Nigel finished inserted at:");

        setUserField(TRAINING_KEY, trainingComplete);
        return this;
    }

    public EnumSet<TrainingCourse> getTrainingCompleted() {

        ArrayList<TrainingCourse> trainingCompleted = (ArrayList<TrainingCourse>) getUserField(TRAINING_KEY);
        if (trainingCompleted == null) {
            return EnumSet.noneOf(TrainingCourse.class);
        }
        EnumSet<TrainingCourse> trainingCourses = EnumSet.noneOf(TrainingCourse.class);
        System.out.println("Nigel training courses");
        System.out.println(trainingCompleted.getClass());
        System.out.println(trainingCompleted);


        for (TrainingCourse training : trainingCompleted) {
            trainingCourses.add(training);
        }

        return trainingCourses;
    }

    public void setTrainingCompleted(final TrainingCourse... trainingCompleted) {
        trainingCompleted(trainingCompleted);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ExampleUser exampleUser = (ExampleUser) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 19)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
