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

package uk.gov.gchq.palisade.example.hrdatagenerator.types;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Random;

public class PhoneNumber {
    private String type; // is this a home number, work number, mobile number ...
    private String phoneNumber;
    private static final String[] DEFAULT_TYPES = new String[]{"Mobile"};
    private static final String[] POSSIBLE_TYPES = new String[]{"Home", "Work", "Work Mobile"};

    public static PhoneNumber[] generateMany(final Random random) {
        int numberOfExtraContacts = random.nextInt(3);
        PhoneNumber[] phoneNumbers = new PhoneNumber[numberOfExtraContacts + 1];
        phoneNumbers[0] = PhoneNumber.generate(random, DEFAULT_TYPES);
        for (int i = 1; i <= numberOfExtraContacts; i++) {
            phoneNumbers[i] = PhoneNumber.generate(random, POSSIBLE_TYPES);
        }
        return phoneNumbers;
    }

    public static PhoneNumber generate(final Random random) {
        return PhoneNumber.generate(random, POSSIBLE_TYPES);
    }

    private static PhoneNumber generate(final Random random, final String[] possibleTypes) {
        PhoneNumber phoneNumber = new PhoneNumber();
        if (possibleTypes.length == 0) {
            phoneNumber.setType(possibleTypes[0]);
        } else {
            phoneNumber.setType(possibleTypes[random.nextInt(possibleTypes.length)]);
        }
        StringBuilder randomNumber = new StringBuilder("0" + random.nextInt(Integer.MAX_VALUE));
        while (randomNumber.length() < 11) {
            randomNumber.insert(0, "0");
        }
        phoneNumber.setPhoneNumber(randomNumber.toString());
        return phoneNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("phone number", phoneNumber)
                .toString();
    }
}
