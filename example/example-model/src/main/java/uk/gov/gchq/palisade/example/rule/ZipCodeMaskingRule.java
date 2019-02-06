package uk.gov.gchq.palisade.example.rule;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Address;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.rule.Rule;

import java.util.Objects;
import java.util.Set;

public class ZipCodeMaskingRule implements Rule<Employee> {

    public ZipCodeMaskingRule() {
    }

    private Employee maskRecord(Employee maskedRecord) {

        Address address = maskedRecord.getAddress();
        String zipCode = address.getZipCode();
        String zipCodeRedacted = zipCode.substring(0,zipCode.length()-1) + "*";

        address.setStreetAddressNumber(null);
        address.setStreetName(null);
        address.setCity(null);
        address.setZipCode(zipCodeRedacted);

        return maskedRecord;
    }

    public Employee apply( Employee record, User user, Context context) {

        if (null == record) {
            return null;
        }

        Objects.requireNonNull(user);

        Set<String> roles = user.getRoles();


        if (roles.contains("Estates")) {
            return maskRecord(record);
        }
        return record;
    }


}
