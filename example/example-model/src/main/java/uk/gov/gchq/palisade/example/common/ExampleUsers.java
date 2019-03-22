package uk.gov.gchq.palisade.example.common;

import uk.gov.gchq.palisade.User;

public final class ExampleUsers {


    final User alice = new User()
            .userId("Alice")
            .auths("public", "private")
            .roles(Role.HR.name(), Role.PAYROLL.name());

    final User bob = new User()
            .userId("Bob")
            .auths("public")
            .roles(Role.ESTATES.name());

    final User eve = new User()
            .userId("Eve")
            .auths("public")
            .roles(Role.IT.name());




}
