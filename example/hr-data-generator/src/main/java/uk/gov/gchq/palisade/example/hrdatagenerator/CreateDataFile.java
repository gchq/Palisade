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

import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.io.BytesSuppliedInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.Callable;
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
        try {
            int bufferSize = 32;
            AvroSerialiser<Employee> employeeAvroSerialiser = new AvroSerialiser<>(Employee.class);
            Stream<Employee> employeeStream = generateStreamOfEmployees();
            BytesSuppliedInputStream in = (BytesSuppliedInputStream) employeeAvroSerialiser.serialise(employeeStream);
            outputFile.getParentFile().mkdirs();
            OutputStream out = new FileOutputStream(outputFile);
            byte[] buffer = new byte[bufferSize];
            int dataAvailable = in.read(buffer);
            while (dataAvailable > 0) {
                if (dataAvailable > bufferSize) {
                    out.write(buffer);
                } else {
                    out.write(buffer, 0, dataAvailable);
                }
                dataAvailable = in.read(buffer);
            }
        } catch (final Exception ignore) {
        }
        return Boolean.TRUE;
    }

    private Stream<Employee> generateStreamOfEmployees() {
        return Stream.generate(() -> Employee.generate(random)).limit(numberOfEmployees);
    }

}
