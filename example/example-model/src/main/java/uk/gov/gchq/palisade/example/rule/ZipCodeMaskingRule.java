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

package uk.gov.gchq.palisade.example.rule;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.example.common.EmployeeUtils;
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.common.Role;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Address;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Manager;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.WorkLocation;
import uk.gov.gchq.palisade.rule.Rule;

import java.util.Objects;
import java.util.Set;

public class ZipCodeMaskingRule implements Rule<Employee> {

    public ZipCodeMaskingRule() {
    }

    private Employee maskRecord(final Employee maskedRecord) {
        Address address = maskedRecord.getAddress();
        WorkLocation location = maskedRecord.getWorkLocation();
        Address workAddress = location.getAddress();
        String zipCode = address.getZipCode();
        String zipCodeRedacted = zipCode.substring(0, zipCode.length() - 1) + "*";
        String workZipCode = workAddress.getZipCode();
        String redactedWorkZipCode = workZipCode.substring(0, workZipCode.length() - 1) + "*";
        address.setStreetAddressNumber(null);
        address.setStreetName(null);
        address.setZipCode(zipCodeRedacted);
        workAddress.setStreetAddressNumber(null);
        workAddress.setStreetName(null);
        workAddress.setZipCode(redactedWorkZipCode);
        location.setAddress(workAddress);
        return maskedRecord;
    }

    private Employee maskWholeAddress(final Employee maskedRecord) {
        maskedRecord.setAddress(null);
        return maskedRecord;
    }

    public Employee apply(final Employee record, final User user, final Context context) {
        if (null == record) {
            return null;
        }
        Objects.requireNonNull(user);
        Objects.requireNonNull(context);
        UserId userId = user.getUserId();
        Manager[] managers = record.getManager();
        Set<String> roles = user.getRoles();
        String purpose = context.getPurpose();

        if (roles.contains(Role.HR.name())) {
            return record;
        }

        if ((EmployeeUtils.isManager(managers, userId).equals(Boolean.TRUE)) & purpose.equals(Purpose.DUTY_OF_CARE.name())) {
            return record;
        }

        if (roles.contains(Role.ESTATES.name())) {
            return maskRecord(record);
        } else {
            return maskWholeAddress(record);
        }
    }
}




