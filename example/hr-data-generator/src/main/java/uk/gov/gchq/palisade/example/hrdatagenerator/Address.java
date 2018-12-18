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

import com.github.javafaker.Faker;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Locale;

public class Address {
    private static final Faker FAKER = new Faker(new Locale("en-GB"));
    private String fullAddress;

    public static Address generate() {
        Address address = new Address();
        String fullAddress = FAKER.address().fullAddress();
        address.setFullAddress(fullAddress);
        return address;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(final String fullAddress) {
        this.fullAddress = fullAddress;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("fullAddress", fullAddress)
                .toString();
    }
}
