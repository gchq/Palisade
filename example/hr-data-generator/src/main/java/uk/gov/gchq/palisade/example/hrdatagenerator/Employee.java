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

import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Random;

public class Employee {

    private int uid;
    private Name name;
    private String dateOfBirth;
    private PhoneNumber[] contactNumbers;
    private EmergencyContact[] emergencyContacts;
    private Address address;
    private BankDetails bankDetails;
    private String taxCode;
    private Nationality nationality;
    private Manager[] manager;

    public static Employee generate(final Random random) {
        Employee employee = new Employee();
        employee.setUid(generateUID(random));
        employee.setName(new NameGenerator().generateName());
        employee.setDateOfBirth(DateHelper.generateDateOfBirth(random));
        employee.setContactNumbers(PhoneNumber.generateMany(random));
        employee.setEmergencyContacts(EmergencyContact.generateMany(random));
        employee.setAddress(Address.generate());
        employee.setBankDetails(BankDetails.generate(random));
        employee.setTaxCode(generateTaxCode());
        employee.setNationality(Nationality.generate(random));
        employee.setManager(Manager.generateMany(random, random.nextInt(3) + 2));
        return employee;
    }

    private static int generateUID(final Random random) {
        return random.nextInt();
    }

    private static String generateTaxCode() {
        return "11500L";
    }

    public int getUid() {
        return uid;
    }

    public void setUid(final int uid) {
        this.uid = uid;
    }

    public Name getName() {
        return name;
    }

    public void setName(final Name name) {
        this.name = name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public PhoneNumber[] getContactNumbers() {
        return contactNumbers;
    }

    public void setContactNumbers(final PhoneNumber[] contactNumbers) {
        this.contactNumbers = contactNumbers;
    }

    public EmergencyContact[] getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(final EmergencyContact[] emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public BankDetails getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(final BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(final String taxCode) {
        this.taxCode = taxCode;
    }

    public Nationality getNationality() {
        return nationality;
    }

    public void setNationality(final Nationality nationality) {
        this.nationality = nationality;
    }

    public Manager[] getManager() {
        return manager;
    }

    public void setManager(final Manager[] manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uid", uid)
                .append("name", name)
                .append("dateOfBirth", dateOfBirth)
                .append("contactNumbers", contactNumbers)
                .append("emergencyContacts", emergencyContacts)
                .append("address", address)
                .append("bankDetails", bankDetails)
                .append("taxCode", taxCode)
                .append("nationality", nationality)
                .append("manager", manager)
                .toString();
    }
}
