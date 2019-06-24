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

import org.junit.Test;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.common.ExampleUser;
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.common.Role;
import uk.gov.gchq.palisade.example.common.TrainingCourse;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.BankDetails;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BankDetailsRulesTest {

    private static final Employee TEST_EMPLOYEE = Employee.generate(new Random(1));
    private static final User TEST_USER_NOT_PAYROLL = new User().roles("Not Payroll").userId("UserId"); // Role not in Payroll
    private static final User TEST_USER_PAYROLL = new User().roles(Role.PAYROLL.name()).userId("UserId"); // Role in Payroll
    private static final BankDetailsRule BANK_DETAILS_RULE = new BankDetailsRule();
    private static final Context SALARY_CONTEXT = new Context().purpose(Purpose.SALARY.name());
    private static final Context NOT_SALARY_CONTEXT = new Context().purpose("Not Salary");

    @Test
    public void shouldNotRedactForPayrollAndSalary() {
        // Given - Employee, Role, Reason

        // When
        Employee actual = BANK_DETAILS_RULE.apply(TEST_EMPLOYEE, TEST_USER_PAYROLL, SALARY_CONTEXT);
        BankDetails actual_bank_details = actual.getBankDetails();

        // Then
        assertEquals(TEST_EMPLOYEE.getBankDetails(), actual_bank_details);
    }

    @Test
    public void shouldRedactForPayrollAndNotSalary() {
        // Given - Employee, Role, Reason

        // When
        Employee actual = BANK_DETAILS_RULE.apply(TEST_EMPLOYEE, TEST_USER_PAYROLL, NOT_SALARY_CONTEXT);

        // Then
        assertNull(actual.getBankDetails());
    }

    @Test
    public void shouldRedactForNotPayrollAndSalary() {
        // Given - Employee, Role, Reason

        // When
        Employee actual = BANK_DETAILS_RULE.apply(TEST_EMPLOYEE, TEST_USER_NOT_PAYROLL, SALARY_CONTEXT);

        // Then
        assertNull(actual.getBankDetails());
    }

    @Test
    public void shouldRedactForNotPayrollAndNotSalary() {
        // Given - Employee, Role, Reason

        // When
        Employee actual = BANK_DETAILS_RULE.apply(TEST_EMPLOYEE, TEST_USER_NOT_PAYROLL, NOT_SALARY_CONTEXT);

        // Then
        assertNull(actual.getBankDetails());
    }

    @Test
    public void shouldDeserialiseExampleUser() {
        //given
        User user = new ExampleUser().trainingCompleted(TrainingCourse.PAYROLL_TRAINING_COURSE).userId("bob").roles("payroll", "something").auths("authorised_person", "whatever");

        //when
        byte[] bytesSerialised = JSONSerialiser.serialise(user, true);
        String serialised = new String(bytesSerialised);
        User newUser = JSONSerialiser.deserialise(bytesSerialised, User.class);

        //then
        assertEquals(newUser.getClass(), ExampleUser.class);
        ExampleUser exampleUser = (ExampleUser) newUser;
        assertEquals(exampleUser.getTrainingCompleted().size(), 1);
        assertTrue("Contains Payroll_training", exampleUser.getTrainingCompleted().contains(TrainingCourse.PAYROLL_TRAINING_COURSE));
    }
}
