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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DumpDataFile {

    private final File inputFile;
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpData.class);

    public DumpDataFile(final File inputFile) throws Exception {
        this.inputFile = inputFile;

        try {
            AvroSerialiser<Employee> employeeAvroSerialiser = new AvroSerialiser<>(Employee.class);
            InputStream in = new FileInputStream(inputFile);
            Stream<Employee> output = employeeAvroSerialiser.deserialise(in);
            List<Employee> employees = output.collect(Collectors.toList());
            LOGGER.info(employees.toString());
        } catch (Exception e) {
            LOGGER.info("Caught exception: " + e.getMessage());
            throw e;
        }
    }
}
