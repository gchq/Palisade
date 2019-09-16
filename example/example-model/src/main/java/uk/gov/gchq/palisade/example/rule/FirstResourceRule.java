/*
 * Copyright 2019 Crown Copyright
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

package uk.gov.gchq.palisade.example.rule;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.common.Role;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.rule.Rule;

import java.util.Objects;
import java.util.Set;

public class FirstResourceRule implements Rule<Resource> {

    public FirstResourceRule() {
    }

    public Resource apply(final Resource resource, final User user, final Context context) {

        Objects.requireNonNull(user);
        Objects.requireNonNull(context);
        Set<String> roles = user.getRoles();
        String fileId = resource.getId();
        String fileName = removeFileExtension(fileId);
        String lastChar = fileName.substring(fileName.length() - 1);

        if (lastChar.equals("1")) {
            if (roles.contains(Role.HR.name())) {
                return resource;
            } else {
                return null;
            }
        } else {
            return resource;
        }
    }

    private String removeFileExtension(final String fileId) {
        return fileId.substring(0, fileId.lastIndexOf('.'));
    }

}
