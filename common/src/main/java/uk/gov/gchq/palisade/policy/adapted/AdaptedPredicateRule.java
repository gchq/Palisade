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
package uk.gov.gchq.palisade.policy.adapted;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;
import uk.gov.gchq.koryphe.adapted.InputAdapted;
import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.PredicateRule;

import java.util.function.Function;

@Since("0.0.1")
@Summary("Adapts the input and applies a predicate rule")
public abstract class AdaptedPredicateRule<I, PI> extends InputAdapted<I, PI> implements PredicateRule<I> {
    @JsonTypeInfo(
            use = Id.CLASS,
            include = As.PROPERTY,
            property = "class"
    )
    protected PredicateRule<PI> predicate;

    public AdaptedPredicateRule() {
    }

    public AdaptedPredicateRule(final Function<I, PI> inputAdapter, final PredicateRule<PI> predicate) {
        this.setInputAdapter(inputAdapter);
        this.setPredicate(predicate);
    }

    @Override
    public boolean test(final I record, final User user, final Justification justification) {
        return null == this.predicate || this.predicate.test(this.adaptInput(record), user, justification);
    }

    public PredicateRule<PI> getPredicate() {
        return this.predicate;
    }

    public void setPredicate(final PredicateRule<PI> predicate) {
        this.predicate = predicate;
    }
}
