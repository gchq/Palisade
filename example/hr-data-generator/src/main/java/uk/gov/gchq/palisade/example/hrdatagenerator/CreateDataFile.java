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

package uk.gov.gchq.palisade.example.hrdatagenerator;

import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public final class CreateDataFile implements Callable<Boolean> {

    private final long numberOfEmployees;
    private final Random random;
    private final File outputFile;

    public CreateDataFile(final long numberOfEmployees, final long seed, final File outputFile) {
        this.numberOfEmployees = numberOfEmployees;
        this.random = new Random(seed);
        this.outputFile = outputFile;
    }

    public Boolean call() {
        outputFile.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(outputFile)) {
            Stream<Employee> employeeStream;
            AvroSerialiser<Employee> employeeAvroSerialiser = new AvroSerialiser<>(Employee.class);
            // Need one Employee whose manager has a UID of Bob (for examples to work)
            Employee firstEmployee = Employee.generate(random);
            Manager[] managers = firstEmployee.getManager();
            UserId lineManagerUid = managers[0].getUid();
            lineManagerUid.setId("Eve");
            managers[0].setUid(lineManagerUid);
            firstEmployee.setManager(managers);
            Stream<Employee> firstEmployeeStream = Stream.of(firstEmployee);
            if (numberOfEmployees > 1) {
                Stream<Employee> moreEmployeesStream = generateStreamOfEmployees();
                employeeStream = Stream.concat(firstEmployeeStream, moreEmployeesStream);
            } else {
                employeeStream = firstEmployeeStream;
            }

            employeeAvroSerialiser.serialise(employeeStream, out);
        } catch (final Exception error) {
            error.printStackTrace();
        }
        return Boolean.TRUE;
    }

    private Stream<Employee> generateStreamOfEmployees() {
        final AtomicLong counter = new AtomicLong(0);
        final long countWrite = numberOfEmployees / 10;
        return Stream.generate(() -> {
            long count = counter.incrementAndGet();
            if ((numberOfEmployees > 1_000_000 && countWrite > 0 && count % countWrite == 0) || count % 100_000 == 0) {
                System.err.printf("Thread %s has written %d records.%n", Thread.currentThread().getName(), count);
            }
            return Employee.generate(random);
        }).limit(numberOfEmployees - 1);
    }

}
