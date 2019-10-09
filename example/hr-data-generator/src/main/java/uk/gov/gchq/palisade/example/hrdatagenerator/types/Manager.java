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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.gov.gchq.palisade.UserId;

import java.util.Random;

public class Manager {
    private UserId uid;
    private Manager[] manager;
    private String managerType;

    public Manager() {
    }

    public Manager(final Manager otherManager) {
        managerType = otherManager.managerType;

        if (otherManager.uid != null) {
            uid = otherManager.uid.clone();
        }

        // Nested
        if (otherManager.manager != null) {
            int arrayLen = otherManager.manager.length;
            manager = new Manager[arrayLen];
            for (int i = 0; i < arrayLen; i++) {
                if (otherManager.manager[i] != null) {
                    manager[i] = new Manager(otherManager.manager[i]);
                }
            }
        }
    }

    public static Manager[] generateMany(final Random random, final int chain) {
        Manager[] managers = new Manager[3];
        managers[0] = Manager.generateRecursive(random, chain, "Line Manager");
        managers[1] = Manager.generateRecursive(random, chain, "Task Manager");
        managers[2] = Manager.generateRecursive(random, chain, "Career Manager");
        return managers;
    }


    public static Manager generateRecursive(final Random random, final int chain, final String managerType) {
        Manager manager = Manager.generate(random, managerType);
        if (chain <= 1) {
            manager.setManager(null);
        } else {
            manager.setManager(Manager.generateMany(random, chain - 1));
        }
        return manager;
    }

    public static Manager generate(final Random random, final String managerType) {
        Manager manager = new Manager();
        manager.setUid(Employee.generateUID(random));
        manager.setManagerType(managerType);

        return manager;
    }

    public String getManagerType() {
        return managerType;
    }

    public void setManagerType(final String managerType) {
        this.managerType = managerType;
    }

    public UserId getUid() {
        return uid;
    }

    public void setUid(final UserId uid) {
        this.uid = uid;
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
                .append("manager", manager)
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

        final Manager otherManager = (Manager) o;

        return new EqualsBuilder()
                .append(uid, otherManager.uid)
                .append(manager, otherManager.manager)
                .append(managerType, otherManager.managerType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 43)
                .append(uid)
                .append(manager)
                .append(managerType)
                .toHashCode();
    }
}
