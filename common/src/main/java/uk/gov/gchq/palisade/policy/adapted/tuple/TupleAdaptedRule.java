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
import uk.gov.gchq.koryphe.tuple.TupleOutputAdapter;
import uk.gov.gchq.palisade.policy.Rule;
import uk.gov.gchq.palisade.policy.adapted.AdaptedRule;

/**
 * A <code>TupleAdaptedRule</code> adapts a {@link Rule} so it can be applied to selected
 * fields from a {@link Tuple}, projecting it's output back into the tuple.
 *
 * @param <R> Reference type used by tuples
 * @param <T> Output type of the Function
 * @see TupleInputAdapter
 * @see TupleOutputAdapter
 */
@JsonPropertyOrder(value = {"class", "selection", "rule", "projection"}, alphabetic = true)
@Since("0.0.1")
@Summary("Applies a rule and adapts the input/output")
public class TupleAdaptedRule<R, T> extends AdaptedRule<Tuple<R>, T> {
    /**
     * Default - for serialisation.
     */
    public TupleAdaptedRule() {
        setInputAdapter(new TupleInputAdapter<>());
        setOutputAdapter(new TupleOutputAdapter<>());
    }

    public TupleAdaptedRule(final Rule<T> rule) {
        this();
        setRule(rule);
    }

    public TupleAdaptedRule(final Rule<T> rule, final R[] selection, final R[] projection) {
        this();
        setRule(rule);
        setSelection(selection);
        setProjection(projection);
    }

    public R[] getSelection() {
        return getInputAdapter().getSelection();
    }

    public void setSelection(final R[] selection) {
        getInputAdapter().setSelection(selection);
    }

    public R[] getProjection() {
        return getOutputAdapter().getProjection();
    }

    public void setProjection(final R[] projection) {
        getOutputAdapter().setProjection(projection);
    }

    @JsonIgnore
    @Override
    public TupleInputAdapter<R, T> getInputAdapter() {
        return (TupleInputAdapter<R, T>) super.getInputAdapter();
    }

    @JsonIgnore
    @Override
    public TupleOutputAdapter<R, T> getOutputAdapter() {
        return (TupleOutputAdapter<R, T>) super.getOutputAdapter();
    }
}
