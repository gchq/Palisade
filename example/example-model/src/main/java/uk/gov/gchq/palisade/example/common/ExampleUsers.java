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

    public static User getAlice() {
        final User alice = new User().userId("Alice")
                .auths("public", "private")
                .roles(Role.HR.name(), Role.PAYROLL.name());
        return (alice);
    }

    public User getBob() {
        final User bob = new User().userId("Bob")
                .auths("public")
                .roles(Role.ESTATES.name());
        return (bob);
    }

    public User getEve() {
        final User eve = new User().userId("Eve")
                .auths("public")
                .roles(Role.IT.name());
        return (eve);
    }
}
