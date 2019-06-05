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
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.common.Role;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.BankDetails;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BankDetailsTest {

    private static final Employee TEST_EMPLOYEE = Employee.generate(new Random(1));
    private static final User TEST_USER_NOT_PAYROLL = new User().roles("Not Payroll").userId("User Id"); // Role not in Payroll
    private static final User TEST_USER_PAYROLL = new User().roles(Role.PAYROLL.name()).userId("User Id"); // Role in Payroll
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
}
