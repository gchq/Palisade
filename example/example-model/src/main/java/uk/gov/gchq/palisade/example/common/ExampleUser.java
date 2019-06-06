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

import java.util.Arrays;
import java.util.EnumSet;

import static java.util.Objects.requireNonNull;

public class ExampleUser extends User {

    private EnumSet<TrainingCourse> trainingCourses = EnumSet.noneOf(TrainingCourse.class);

    public ExampleUser(final User user) {
        setUserId(user.getUserId());
        setAuths(user.getAuths());
        setRoles(user.getRoles());
    }

    public ExampleUser() {
    }

    public ExampleUser trainingCompleted(final TrainingCourse... trainingCompleted) {
        requireNonNull(trainingCompleted, "cannot add null training completed");
        trainingCourses.clear();
        trainingCourses.addAll(Arrays.asList(trainingCompleted));
        return this;
    }

    public EnumSet<TrainingCourse> getTrainingCompleted() {
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
                .append(trainingCourses, exampleUser.trainingCourses)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 19)
                .appendSuper(super.hashCode())
                .append(trainingCourses)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("trainingCourses", trainingCourses)
                .toString();
    }
}
