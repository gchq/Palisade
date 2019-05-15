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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

//@JsonIgnoreProperties(value = {"trainingCompleted"})
public class ExampleUser extends User {

    private Set<TrainingCourse> trainingCompleted = new HashSet<>();

    public ExampleUser() {
    }

    public ExampleUser trainingCompleted(final TrainingCourse... trainingCompleted) {
        requireNonNull(trainingCompleted, "cannot add null training completed");
        Collections.addAll(this.trainingCompleted, trainingCompleted);
        return this;
    }

    public Set<TrainingCourse> getTrainingCompleted() {
        requireNonNull(trainingCompleted, "trainingCompleted cannot be null");
        return trainingCompleted;
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
                .append(trainingCompleted, exampleUser.trainingCompleted)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 19)
                .appendSuper(super.hashCode())
                .append(trainingCompleted)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("trainingCompleted", trainingCompleted)
                .toString();
    }


}
