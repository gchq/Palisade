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

import com.github.javafaker.Faker;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Random;

public class Address {

    private String streetAddressNumber;
    private String streetName;
    private String city;
    private String state;
    private String zipCode;

    public static Address generate(final Faker faker, final Random random) {
        Address address = new Address();
        com.github.javafaker.Address fakeAddress = faker.address();
        address.setStreetAddressNumber(fakeAddress.streetAddressNumber());
        address.setStreetName(fakeAddress.streetName());
        address.setCity(fakeAddress.city());
        address.setState(fakeAddress.state());
        address.setZipCode(fakeAddress.zipCode());
        return address;
    }

    public String getStreetAddressNumber() {
        return streetAddressNumber;
    }

    public void setStreetAddressNumber(final String streetAddressNumber) {
        this.streetAddressNumber = streetAddressNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(final String streetName) {
        this.streetName = streetName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("streetAddressNumber", streetAddressNumber)
                .append("streetName", streetName)
                .append("city", city)
                .append("state", state)
                .append("zipCode", zipCode)
                .toString();
    }
}
