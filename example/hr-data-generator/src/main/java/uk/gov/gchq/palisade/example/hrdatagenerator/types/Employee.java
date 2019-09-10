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
import com.github.javafaker.Name;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.example.hrdatagenerator.utils.DateHelper;

import java.util.Random;

public class Employee {

    private UserId uid;
    private String name;
    private String dateOfBirth;
    private PhoneNumber[] contactNumbers;
    private EmergencyContact[] emergencyContacts;
    private Address address;
    private BankDetails bankDetails;
    private String taxCode;
    private Nationality nationality;
    private Manager[] manager;
    private String hireDate;
    private Grade grade;
    private Department department;
    private int salaryAmount;
    private int salaryBonus;
    private WorkLocation workLocation;
    private Sex sex;


    public static Employee generate(final Random random) {
        Employee employee = new Employee();
        Faker faker = ThreadLocalFaker.getFaker(random);
        employee.setUid(generateUID(random));
        Name employeeName = faker.name();
        employee.setName(employeeName.firstName() + " " + employeeName.lastName()); // we are storing name as a string not a Name
        employee.setDateOfBirth(DateHelper.generateDateOfBirth(random));
        employee.setContactNumbers(PhoneNumber.generateMany(random));
        employee.setEmergencyContacts(EmergencyContact.generateMany(faker, random));
        employee.setAddress(Address.generate(faker, random));
        employee.setBankDetails(BankDetails.generate(random));
        employee.setTaxCode(generateTaxCode());
        employee.setNationality(Nationality.generate(random));
        employee.setManager(Manager.generateMany(random, random.nextInt(3) + 2));
        employee.setHireDate(DateHelper.generateHireDate(employee.dateOfBirth, random));
        employee.setGrade(Grade.generate(random));
        employee.setDepartment(Department.generate(random));
        employee.setSalaryAmount(20000 + random.nextInt(1000000));
        employee.setSalaryBonus(random.nextInt(10000));
        employee.setWorkLocation(WorkLocation.generate(faker, random));
        employee.setSex(Sex.generate(random));

        return employee;
    }

    public static UserId generateUID(final Random random) {
        return new UserId().id(String.valueOf(random.nextInt(Integer.MAX_VALUE)));
    }

    private static String generateTaxCode() {
        return "11500L";
    }

    public UserId getUid() {
        return uid;
    }

    public void setUid(final UserId uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
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

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(final String hireDate) {
        this.hireDate = hireDate;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(final Grade grade) {
        this.grade = grade;
    }

    public int getSalaryAmount() {
        return salaryAmount;
    }

    public void setSalaryAmount(final int salaryAmount) {
        this.salaryAmount = salaryAmount;
    }

    public int getSalaryBonus() {
        return salaryBonus;
    }

    public void setSalaryBonus(final int salaryBonus) {
        this.salaryBonus = salaryBonus;
    }

    public void setDepartment(final Department department) {
        this.department = department;
    }

    public WorkLocation getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(final WorkLocation workLocation) {
        this.workLocation = workLocation;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(final Sex sex) {
        this.sex = sex;
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
                .append("hireDate", hireDate)
                .append("grade", grade)
                .append("department", department)
                .append("salaryAmount", salaryAmount)
                .append("salaryBonus", salaryBonus)
                .append("workLocation", workLocation)
                .append("sex", sex)
                .toString();
    }
}
