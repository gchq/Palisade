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

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Random;

public class Manager {
    private int uid;
    private Manager manager;

    public static Manager generateRecursive(final Random random, final int chain) {
        Manager manager = new Manager();
        manager.setUid(generateUID(random));
        if (chain <= 1) {
            manager.setManager(null);
        }
        else {
            manager.setManager(Manager.generateRecursive(random, chain - 1));
        }
        return manager;
    }

    public static Manager generate(final Random random) {
        int chain = random.nextInt(3 ) + 2;
        Manager manager = new Manager();
        manager.setUid(generateUID(random));
        manager.setManager(generateRecursive(random,chain-1));
        return manager;
    }

    private static int generateUID(final Random random) {
        return random.nextInt();
    }

    public int getUid() {
        return uid;
    }

    public void setUid(final int uid) {
        this.uid = uid;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(final Manager manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uid", uid)
                .append("manager", manager)
                .toString();
    }
}
