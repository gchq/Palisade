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
    private static final ThreadLocal<GregorianCalendar> GREGORIAN_CALENDAR = ThreadLocal.withInitial(() -> new GregorianCalendar());

    private DateHelper() {
    }

    public static String generateDateOfBirth(final Random random) {
        GregorianCalendar localCalendar = GREGORIAN_CALENDAR.get();
        int year = 1800 + random.nextInt(100);
        localCalendar.set(localCalendar.YEAR, year);
        int dayOfYear = random.nextInt(localCalendar.getActualMaximum(localCalendar.DAY_OF_YEAR));
        localCalendar.set(localCalendar.DAY_OF_YEAR, dayOfYear);
        return localCalendar.get(localCalendar.DAY_OF_MONTH) + "/" + (localCalendar.get(localCalendar.MONTH) + 1) + "/" + year;
    }

    public static String generateHireDate(final String dateOfBirthStr, final Random random) {
        String birthYearStr = dateOfBirthStr.substring(dateOfBirthStr.length() - 4);
        String hireDateStr = dateOfBirthStr.substring(0, dateOfBirthStr.length() - 4);

        int birthYear = new Integer(birthYearStr).intValue();
        int hireYear = birthYear + 20 + random.nextInt(40);

        hireDateStr = hireDateStr + hireYear;

        return hireDateStr;

    }
}
