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

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class EmployeeTest {

    private static Random random = new Random(1);

    // While this can be done by annotating generateEmployeeDataPoints directly, this results in a big performance hit
    // as the (expensive) generate function is called for each data point for each argument (i.e. x1, x6, x31, x156, ...)
    // This is a known but unaddressed JUnit bug.
    // Instead, 'cache' the result of the function call.
    private static Employee[] generateEmployeeDataPoints() {
        ArrayList<Employee> dataPoints = new ArrayList<Employee>();
        // Identical copies of Employee
        for (int i = 0; i < 3; i++) {
            dataPoints.add(Employee.generate(new Random(1)));
        }
        // Uniquely generated Employees
        Random random = new Random(2);
        for (int i = 0; i < 2; i++) {
            dataPoints.add(Employee.generate(random));
        }
        return dataPoints.toArray(new Employee[dataPoints.size()]);
    }
    @DataPoints
    public static final Employee[] employeeDataPoints = generateEmployeeDataPoints();

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
    public void testCopyConstructor(Employee x) {
        // Given
        Employee copy = new Employee(x);
        // Then
        assertThat(x, equalTo(copy));
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
    public void testEqualHashCodeWhenCopied(Employee x) {
        // Given
        Employee copy = new Employee(x);
        // Then
        assertThat(x.hashCode(), equalTo(copy.hashCode()));
    }

    @Theory
    public void testEqualHashCodeWhenEqual(Employee x, Employee y) {
        // Given
        assumeThat(x, equalTo(y));
        // Then
        assertThat(x.hashCode(), equalTo(y.hashCode()));
    }

}
