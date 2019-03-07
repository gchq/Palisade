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
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.common.Role;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Manager;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DutyOfCareTest {

    private static final Employee TEST_EMPLOYEE = Employee.generate(new Random(1));
    private static final User TEST_USER1 = new User().userId(new UserId().id("1962720332")).roles("Not HR"); // Start of chain and not in HR
    private static final User TEST_USER2 = new User().userId(new UserId().id("1816031731")).roles("Not HR"); // Middle of chain and not HR
    private static final User TEST_USER3 = new User().userId(new UserId().id("1501105288")).roles("Not HR"); // End of chain and not HR
    private static final User TEST_USER4 = new User().userId(new UserId().id("1")).roles(Role.HR.name()); // Not in chain and HR
    private static final User TEST_USER5 = new User().userId(new UserId().id("1")).roles("Not HR"); // Not in chain and HR
    private static final Manager[] managers = TEST_EMPLOYEE.getManager();
    private static final DutyOfCareRule DUTY_OF_CARE_RULE = new DutyOfCareRule();
    private static final Context DUTY_OF_CARE_CONTEXT = new Context().purpose(Purpose.DUTY_OF_CARE.name());
    private static final Context NOT_DUTY_OF_CARE_CONTEXT = new Context().purpose("Not Duty of Care");

    @Test
    public void shouldNotRedactForStartOfManagerInChain() {
        // Given - Employee, Role, Reason

        // When
        Employee actual = DUTY_OF_CARE_RULE.apply(TEST_EMPLOYEE, TEST_USER1, DUTY_OF_CARE_CONTEXT);

        // Then
        assertEquals(TEST_EMPLOYEE, actual);
    }

    @Test
    public void shouldNotRedactForMiddleManagerInChain() {
        // Given - Nothing

        // When
        Employee actual = DUTY_OF_CARE_RULE.apply(TEST_EMPLOYEE, TEST_USER2, DUTY_OF_CARE_CONTEXT);

        // Then
        assertEquals(TEST_EMPLOYEE, actual);
    }

    @Test
    public void shouldNotRedactForEndManagerInChain() {
        // Given - Nothing

        // When
        Employee actual = DUTY_OF_CARE_RULE.apply(TEST_EMPLOYEE, TEST_USER3, DUTY_OF_CARE_CONTEXT);

        // Then
        assertEquals(TEST_EMPLOYEE, actual);
    }

    @Test
    public void shouldNotRedactForHRAndDutyOfCare() {
        // Given - Nothing

        // When
        Employee actual = DUTY_OF_CARE_RULE.apply(TEST_EMPLOYEE, TEST_USER4, DUTY_OF_CARE_CONTEXT);

        // Then
        assertEquals(TEST_EMPLOYEE, actual);
    }

    @Test
    public void shouldRedactForNotManagerAndNotHR() {
        // Given - Nothing

        // When
        Employee actual = DUTY_OF_CARE_RULE.apply(TEST_EMPLOYEE, TEST_USER5, DUTY_OF_CARE_CONTEXT);

        // Then
        assertNull(actual.getContactNumbers());
    }

    @Test
    public void shouldRedactForEndManagerInChainNotDutyOfCare() {
        // Given - Nothing

        // When
        Employee actual = DUTY_OF_CARE_RULE.apply(TEST_EMPLOYEE, TEST_USER3, NOT_DUTY_OF_CARE_CONTEXT);

        // Then
        assertNull(actual.getContactNumbers());
    }

    @Test
    public void shouldRedactForHRAndNotDutyOfCare() {
        // Given - Nothing

        // When
        Employee actual = DUTY_OF_CARE_RULE.apply(TEST_EMPLOYEE, TEST_USER4, NOT_DUTY_OF_CARE_CONTEXT);

        // Then
        assertNull(actual.getContactNumbers());
    }

    @Test
    public void shouldRedactForNotManagerAndNotHRAndNotDutyOfCare() {
        // Given - Nothing

        // When
        Employee actual = DUTY_OF_CARE_RULE.apply(TEST_EMPLOYEE, TEST_USER5, NOT_DUTY_OF_CARE_CONTEXT);

        // Then
        assertNull(actual.getContactNumbers());
    }
}
