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
import uk.gov.gchq.palisade.example.common.Role;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RecordMaskingTest {
    private static final Employee TEST_EMPLOYEE = Employee.generate(new Random(1));
    private static final UserId TEST_USERID = new UserId().id("1");
    private static final User TEST_USER_1 = new User().userId(TEST_USERID).roles("A Role"); //Not in chain and is not HR or Estates
    private static final User TEST_USER_2 = new User().userId("1252742854").roles("A Role"); //Start of chain and is not HR or Estates
    private static final User TEST_USER_3 = new User().userId("1830200592").roles("A Role"); //Middle of chain and is not HR or Estates
    private static final User TEST_USER_4 = new User().userId("2788683").roles("A Role"); //End of chain and is not HR or Estates
    private static final User TEST_USER_5 = new User().userId(TEST_USERID).roles(Role.HR.name()); //Not in chain and has HR role
    private static final User TEST_USER_6 = new User().userId(TEST_USERID).roles(Role.ESTATES.name()); //Not in chain and has Estates role
    private static final Context TEST_CONTEXT = new Context().purpose("A purpose");
    private static final RecordMaskingRule RECORD_MASKING_RULE = new RecordMaskingRule();

    @Test
    public void redactionForNonManagerUser() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_1, TEST_CONTEXT);

        //Then
        assertNull(actual);
    }

    /*@Test
    public void partialRedactionForFirstLevelManager() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_2, TEST_CONTEXT);

        //Then
        assertEquals(TEST_EMPLOYEE, actual);
    }

    @Test
    public void partialRedactionForMidLevelManager() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_3, TEST_CONTEXT);

        //Then
        assertEquals(TEST_EMPLOYEE, actual);
    }

    @Test
    public void partialRedactionForEndLevelManager() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_4, TEST_CONTEXT);

        //Then
        assertEquals(TEST_EMPLOYEE, actual);
    }*/

    @Test
    public void fullRedactionForFirstLevelManager() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_2, TEST_CONTEXT);

        //Then
        assertNull(actual);
    }

    @Test
    public void fullRedactionForMidLevelManager() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_3, TEST_CONTEXT);

        //Then
        assertNull(actual);
    }

    @Test
    public void fullRedactionForEndLevelManager() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_4, TEST_CONTEXT);

        //Then
        assertNull(actual);
    }

    @Test
    public void noRedactionForHrRole() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_5, TEST_CONTEXT);

        //Then
        assertEquals(TEST_EMPLOYEE, actual);
    }

    @Test
    public void noRedactionForEstatesRole() {
        //Given - Employee, Role, Reason

        //When
        Employee actual = RECORD_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_6, TEST_CONTEXT);

        //Then
        assertEquals(TEST_EMPLOYEE, actual);
    }

}
