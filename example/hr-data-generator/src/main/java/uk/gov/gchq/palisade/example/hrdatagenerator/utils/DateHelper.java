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

package uk.gov.gchq.palisade.example.hrdatagenerator.utils;

import java.util.GregorianCalendar;
import java.util.Random;

public final class DateHelper {
    private static final GregorianCalendar GREGORIAN_CALENDAR = new GregorianCalendar();

    private DateHelper() {
    }

    public static String generateDateOfBirth(final Random random) {
        int year = 1800 + random.nextInt(100);
        GREGORIAN_CALENDAR.set(GREGORIAN_CALENDAR.YEAR, year);
        int dayOfYear = random.nextInt(GREGORIAN_CALENDAR.getActualMaximum(GREGORIAN_CALENDAR.DAY_OF_YEAR));
        GREGORIAN_CALENDAR.set(GREGORIAN_CALENDAR.DAY_OF_YEAR, dayOfYear);
        return GREGORIAN_CALENDAR.get(GREGORIAN_CALENDAR.DAY_OF_MONTH) + "/" + GREGORIAN_CALENDAR.get(GREGORIAN_CALENDAR.MONTH) + "/" + year;
    }
}
