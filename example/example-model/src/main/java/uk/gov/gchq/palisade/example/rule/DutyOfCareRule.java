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
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Manager;
import uk.gov.gchq.palisade.rule.Rule;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class DutyOfCareRule implements Rule<Employee> {
    public DutyOfCareRule() {
    }

    private Employee redactRecord(final Employee redactedRecord) {
        redactedRecord.setContactNumbers(null);
        redactedRecord.setEmergencyContacts(null);
        redactedRecord.setSex(null);
        return redactedRecord;
    }

    public Employee apply(final Employee record, final User user, final Context context) {
        if (null == record) {
            return null;
        }

        requireNonNull(user);
        requireNonNull(context);
        Set<String> roles = user.getRoles();
        String purpose = context.getPurpose();
        UserId userId = user.getUserId();
        Manager[] managers = record.getManager();

        if (roles.contains(Role.HR.name()) & purpose.equals(Purpose.DUTY_OF_CARE.name())) {
            return record;
        } else if ((EmployeeUtils.isManager(managers, userId).equals(Boolean.TRUE)) & purpose.equals(Purpose.DUTY_OF_CARE.name())) {
            return record;
        } else {
            return redactRecord(record);
        }
    }
}
