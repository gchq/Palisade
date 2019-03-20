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

import org.ajbrown.namemachine.Gender;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Random;

public class EmergencyContact {
    private static final Relation[] MALE_RELATIONS = new Relation[]{Relation.BROTHER, Relation.FATHER, Relation.GRANDFATHER, Relation.SON};
    private static final Relation[] FEMALE_RELATIONS = new Relation[]{Relation.DAUGHTER, Relation.GRANDMOTHER, Relation.MOTHER, Relation.SISTER};

    private String contactName;
    private Relation relation;
    private PhoneNumber[] contactNumbers;

    public static EmergencyContact generate(final Random random, final NameGenerator nameGenerator) {
        EmergencyContact contact = new EmergencyContact();
        Name tempName = nameGenerator.generateName();
        contact.setContactName(tempName.toString());
        Relation[] relations;
        if (tempName.getGender().equals(Gender.MALE)) {
            relations = MALE_RELATIONS;
        } else {
            relations = FEMALE_RELATIONS;
        }

        contact.setRelation(relations[random.nextInt(3)]);
        contact.setContactNumbers(PhoneNumber.generateMany(random));
        return contact;
    }

    public static EmergencyContact[] generateMany(final Random random, final NameGenerator nameGenerator) {
        int numberOfExtraContacts = random.nextInt(4);
        EmergencyContact[] emergencyContacts = new EmergencyContact[numberOfExtraContacts + 1];
        emergencyContacts[0] = EmergencyContact.generate(random, nameGenerator);
        for (int i = 1; i <= numberOfExtraContacts; i++) {
            emergencyContacts[i] = EmergencyContact.generate(random, nameGenerator);
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
}
