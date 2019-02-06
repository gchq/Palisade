package uk.gov.gchq.palisade.example.rule;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.rule.Rule;

public class PostCodeMaskingRule implements Rule<Employee> {
    @Override
    public Employee apply( Employee record, User user, Context context ) {
        return null;
    }
}
