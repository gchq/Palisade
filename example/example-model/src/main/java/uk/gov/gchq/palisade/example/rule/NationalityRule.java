package uk.gov.gchq.palisade.example.rule;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.rule.Rule;

import java.util.Objects;
import java.util.Set;

public class NationalityRule implements Rule<Employee> {


    public NationalityRule() {
    }


    private Employee redactRecord( Employee redactedRecord) {
        redactedRecord.setNationality(null);
        return redactedRecord;
    }

    public Employee apply( Employee record, User user, Context context) {

        if (null == record) {
            return null;
        }

        Objects.requireNonNull(user);
        Objects.requireNonNull(context);
        Set<String> roles = user.getRoles();
        String purpose = context.getJustification();

        if (roles.contains("HR") & purpose.equals("Staff report")) {
            return record;
        }
        return redactRecord(record);
    }
}
