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
package uk.gov.gchq.palisade.policy.adapted.tuple;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;
import uk.gov.gchq.koryphe.tuple.Tuple;
import uk.gov.gchq.koryphe.tuple.TupleInputAdapter;
import uk.gov.gchq.palisade.policy.PredicateRule;
import uk.gov.gchq.palisade.policy.adapted.AdaptedPredicateRule;

@JsonPropertyOrder(
        value = {"class", "selection", "predicate"},
        alphabetic = true
)
@Since("0.0.1")
@Summary("Adapts the input and applies a predicate rule")
public class TupleAdaptedPredicateRule<R, PI> extends AdaptedPredicateRule<Tuple<R>, PI> {
    public TupleAdaptedPredicateRule() {
        this.setInputAdapter(new TupleInputAdapter<>());
    }

    public TupleAdaptedPredicateRule(final PredicateRule<PI> predicate, final R[] selection) {
        this();
        this.setPredicate(predicate);
        this.setSelection(selection);
    }

    public R[] getSelection() {
        return this.getInputAdapter().getSelection();
    }

    public void setSelection(final R[] selection) {
        this.getInputAdapter().setSelection(selection);
    }

    @JsonIgnore
    public TupleInputAdapter<R, PI> getInputAdapter() {
        return (TupleInputAdapter) super.getInputAdapter();
    }
}
