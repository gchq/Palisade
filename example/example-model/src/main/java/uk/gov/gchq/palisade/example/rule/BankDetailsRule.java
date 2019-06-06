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
import uk.gov.gchq.palisade.example.common.ExampleUser;
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.common.Role;
import uk.gov.gchq.palisade.example.common.TrainingCourse;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.rule.Rule;

import java.util.EnumSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class BankDetailsRule implements Rule<Employee> {
    public BankDetailsRule() {
    }

    private Employee redactRecord(final Employee redactedRecord) {
        redactedRecord.setBankDetails(null);
        redactedRecord.setTaxCode(null);
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

        System.out.println("class type");
        System.out.println(user.getClass());


        try {

            if (user.getClass() == ExampleUser.class) {
                ExampleUser exampleUser = (ExampleUser) user;
                if (roles.contains(Role.PAYROLL.name()) && purpose.equals(Purpose.SALARY.name())) {
                    return record;
                }
                EnumSet<TrainingCourse> trainingCompleted = exampleUser.getTrainingCompleted();
                if (trainingCompleted.contains(TrainingCourse.PAYROLL_TRAINING_COURSE) & purpose.equals(Purpose.SALARY.name())) {
                    return record;
                }
            }
        } catch (Exception e) {

            System.out.println(e);
            System.out.println(e.getStackTrace());
            throw e;
        }
        return redactRecord(record);
    }
}
