/*
 * Copyright 2018 Crown Copyright
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

import uk.gov.gchq.palisade.User;

public final class ExampleUsers {

    private ExampleUsers() {
    }

    public static User getUser(final String userId) {
        switch (userId) {
            case "Bob":
                return getBob();
            case "Eve":
                return getEve();
            default:
                return getAlice();
        }
    }

    public static User getAlice() {
        final User alice = new ExampleUser()
                .trainingCompleted(TrainingCourse.PAYROLL_TRAINING_COURSE)
                .userId("Alice")
                .auths("public", "private")
                .roles(Role.HR.name());
        return (alice);
    }

    public static User getBob() {
        final User bob = new ExampleUser()
                .userId("Bob")
                .auths("public")
                .roles(Role.ESTATES.name());
        return (bob);
    }

    public static User getEve() {
        final User eve = new ExampleUser()
                .userId("Eve")
                .auths("public")
                .roles(Role.IT.name());
        return (eve);
    }
}
