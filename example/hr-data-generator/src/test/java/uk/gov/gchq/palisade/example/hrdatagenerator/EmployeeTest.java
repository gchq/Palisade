/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.example.hrdatagenerator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;

import java.io.File;
import java.util.Random;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class EmployeeTest {

    // Equal
    private static Random randomA = new Random(0);
    private static Random randomB = new Random(0);
    private static Random randomC = new Random(0);
    // Not equal
    private static Random randomX = new Random(1);
    private static Random randomY = new Random(2);

    // Equal
    @DataPoint
    public static final Employee aliceA = Employee.generate(randomA);
    @DataPoint
    public static final Employee aliceB = Employee.generate(randomB);
    @DataPoint
    public static final Employee aliceC = Employee.generate(randomC);
    // Not equal
    @DataPoint
    public static final Employee bob = Employee.generate(randomX);
    @DataPoint
    public static final Employee charlie = Employee.generate(randomY);

    @Test
    public void generateEmployee() {
        long startTime = System.currentTimeMillis();
        Random random = new Random(0);
        for (int i = 0; i < 100; i++) {
            Employee t = Employee.generate(random);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Took " + (endTime - startTime) + "ms to create 100 employees");
    }

    @Test
    public void generateData() {
        try {
            CreateData.main(new String[]{".data", "50", "1"});
        } finally {
            FileUtils.deleteQuietly(new File(".data"));
        }
    }

    @Theory
    public void testClone(Employee x) {
        // Given
        Employee cloneOfX = x.clone();
        // Then
        assertThat(x, equalTo(cloneOfX));
    }

    @Theory
    public void testReflexiveEquals(Employee x) {
        // Then
        assertThat(x, equalTo(x));
    }

    @Theory
    public void testNullEquals(Employee x) {
        // Then
        assertThat(x, not(equalTo(nullValue())));
    }

    @Theory
    public void testSymmetricEquals(Employee x, Employee y) {
        // Given
        assumeThat(x, equalTo(y));
        // Then
        assertThat(y, equalTo(x));
    }

    @Theory
    public void testTransitiveEquals(Employee x, Employee y, Employee z) {
        // Given
        assumeThat(x, equalTo(y));
        assumeThat(y, equalTo(z));
        // Then
        assertThat(x, equalTo(z));
    }

    @Theory
    public void testConsistentHashCode(Employee x) {
        // Then
        assertThat(x.hashCode(), equalTo(x.hashCode()));
    }

    @Theory
    public void testEquivalentHashCode(Employee x, Employee y) {
        // Then (using clone)
        assertThat(x.hashCode(), equalTo(x.clone().hashCode()));

        // Given
        assumeThat(x, equalTo(y));
        // Then
        assertThat(x.hashCode(), equalTo(y.hashCode()));
    }

}
