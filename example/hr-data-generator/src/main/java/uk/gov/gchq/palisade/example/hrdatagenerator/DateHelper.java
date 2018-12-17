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

import java.util.GregorianCalendar;
import java.util.Random;

public final class DateHelper {

    private DateHelper() {
    }

    public static String generateDateOfBirth(final Random random) {
        GregorianCalendar gc = new GregorianCalendar();
        int year = 1800 + random.nextInt(100);
        gc.set(gc.YEAR, year);
        int dayOfYear = random.nextInt(gc.getActualMaximum(gc.DAY_OF_YEAR));
        gc.set(gc.DAY_OF_YEAR, dayOfYear);
        return gc.get(gc.DAY_OF_MONTH) + "/" + gc.get(gc.MONTH) + "/" + year;
    }
}
