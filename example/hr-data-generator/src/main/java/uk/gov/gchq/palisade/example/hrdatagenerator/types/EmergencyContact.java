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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Random;

public class EmergencyContact {
    private String contactName;
    private Relation relation;
    private PhoneNumber[] contactNumbers;

    public EmergencyContact() {
    }

    public EmergencyContact(final EmergencyContact emergencyContact) {
        contactName = emergencyContact.contactName;
        relation = emergencyContact.relation;

        // Nested
        if (emergencyContact.contactNumbers != null) {
            int arrayLen = emergencyContact.contactNumbers.length;
            contactNumbers = new PhoneNumber[arrayLen];
            for (int i = 0; i < arrayLen; i++) {
                if (emergencyContact.contactNumbers[i] != null) {
                    contactNumbers[i] = new PhoneNumber(emergencyContact.contactNumbers[i]);
                }
            }
        }
    }

    public static EmergencyContact generate(final Faker faker, final Random random) {
        EmergencyContact contact = new EmergencyContact();
        Name tempName = faker.name();
        contact.setContactName(tempName.firstName() + " " + tempName.lastName());
        contact.setRelation(Relation.values()[random.nextInt(Relation.values().length)]);
        contact.setContactNumbers(PhoneNumber.generateMany(random));
        return contact;
    }

    public static EmergencyContact[] generateMany(final Faker faker, final Random random) {
        int numberOfExtraContacts = random.nextInt(4);
        EmergencyContact[] emergencyContacts = new EmergencyContact[numberOfExtraContacts + 1];
        emergencyContacts[0] = EmergencyContact.generate(faker, random);
        for (int i = 1; i <= numberOfExtraContacts; i++) {
            emergencyContacts[i] = EmergencyContact.generate(faker, random);
        }
        return emergencyContacts;
    }

    public String getContactName() {

        return contactName;
    }

    public void setContactName(final String contactName) {
        this.contactName = contactName;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(final Relation relation) {
        this.relation = relation;
    }

    public PhoneNumber[] getContactNumbers() {
        return contactNumbers;
    }

    public void setContactNumbers(final PhoneNumber[] contactNumbers) {
        this.contactNumbers = contactNumbers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("contactName", contactName)
                .append("relation", relation)
                .append("contactNumbers", contactNumbers)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final EmergencyContact emergencyContact = (EmergencyContact) o;

        return new EqualsBuilder()
                .append(contactName, emergencyContact.contactName)
                .append(relation, emergencyContact.relation)
                .append(contactNumbers, emergencyContact.contactNumbers)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 47)
                .append(contactName)
                .append(relation)
                .append(contactNumbers)
                .toHashCode();
    }
}
