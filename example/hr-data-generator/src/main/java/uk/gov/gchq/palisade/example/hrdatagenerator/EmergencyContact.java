/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.example.hrdatagenerator;

import org.ajbrown.namemachine.Gender;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Random;

public class EmergencyContact {
    private static Relation[] maleRelations = new Relation[]{Relation.BROTHER, Relation.FATHER, Relation.GRANDFATHER, Relation.SON};
    private static Relation[] femaleRelations = new Relation[]{Relation.DAUGHTER, Relation.GRANDMOTHER, Relation.MOTHER, Relation.SISTER};

    private Name contactName;
    private Relation relation;
    private PhoneNumber[] contactNumbers;

    public static EmergencyContact generate(final Random random) {
        EmergencyContact contact = new EmergencyContact();
        Name name = new NameGenerator().generateName();
        contact.setContactName(name);
        Relation[] relations;
        if (name.getGender().equals(Gender.MALE)) {
            relations = maleRelations;
        }
        else {
            relations = femaleRelations;
        }

        contact.setRelation(relations[random.nextInt(3)]);
        contact.setContactNumbers(PhoneNumber.generateMany(random));
        return contact;
    }

    public static EmergencyContact[] generateMany(final Random random) {
        int numberOfExtraContacts = random.nextInt(4);
        EmergencyContact[] emergencyContacts = new EmergencyContact[numberOfExtraContacts + 1];
        emergencyContacts[0] = EmergencyContact.generate(random);
        for (int i = 1; i <= numberOfExtraContacts; i++) {
            emergencyContacts[i] = EmergencyContact.generate(random);
        }
        return emergencyContacts;
    }

    public Name getContactName() {

        return contactName;
    }

    public void setContactName(final Name contactName) {
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

//    public String toJson(final int indent) {
//        String prefix = "";
//        for (int i = 0; i<indent; i++){
//            prefix = prefix + "\t";
//        }
//        StringBuilder stringBuilder = new StringBuilder()
//                .append(prefix + EmergencyContact.class.getName() + "{\n")
//                .append(prefix + "\tcontactName : " +  contactName.toString() + ",")
//                .append(prefix + "\trelation : " +  relation + ",")
//                .append(prefix + "\tcontactNumbers : " +  contactNumbers.toJson(indent+2) + ",");
//    }
}
