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
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Manager;
import uk.gov.gchq.palisade.rule.Rule;

import java.util.Objects;
import java.util.Set;

public class DutyOfCareRule implements Rule<Employee> {

    public DutyOfCareRule() {
    }

    private Boolean isManager(Manager[] managers, UserId userId) {
        if (managers.equals(null)) {
            return false;
        }

        for (Manager manager:managers) {
            if (manager.getUid() == userId) {
                return true;
            }
        }

        for (Manager manager:managers) {
            if (isManager(manager.getManager(), userId)) {
                 return true;
            }
        }
        return false;
    }

    private Employee redactRecord(Employee redactedRecord) {
        redactedRecord.setContactNumbers(null);
        return redactedRecord;
    }

    public Employee apply(Employee record, User user, Context context) {

        if (null == record) {
            return null;
        }

        Objects.requireNonNull(user);
        Objects.requireNonNull(context);
        Set<String> roles = user.getRoles();
        String purpose = context.getJustification();
        UserId userId = user.getUserId();
        Manager[] managers = record.getManager();

        if (roles.contains("HR") & purpose.equals("Duty of Care")) {
            return record;
        } else if (isManager(managers, userId) == Boolean.TRUE) {
            return record;
        } else {
            return redactRecord(record);
        }
    }
}
