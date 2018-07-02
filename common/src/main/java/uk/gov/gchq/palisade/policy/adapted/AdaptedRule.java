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

import uk.gov.gchq.koryphe.adapted.Adapted;
import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.policy.Rule;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An {@link Adapted} {@link Rule}.
 *
 * @param <IO> Input/Output record type
 * @param <T>  Rule type
 */
public abstract class AdaptedRule<IO, T> extends Adapted<IO, T, T, IO, IO> implements Rule<IO> {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    protected Rule<T> rule;

    /**
     * Default - for serialisation.
     */
    public AdaptedRule() {
    }

    public AdaptedRule(final Rule<T> rule,
                       final Function<IO, T> inputAdapter,
                       final BiFunction<IO, T, IO> outputAdapter) {
        setInputAdapter(inputAdapter);
        setRule(rule);
        setOutputAdapter(outputAdapter);
    }

    public AdaptedRule(final Rule<T> rule,
                       final Function<IO, T> inputAdapter,
                       final Function<T, IO> outputAdapter) {
        setInputAdapter(inputAdapter);
        setRule(rule);
        setOutputAdapter(outputAdapter);
    }

    /**
     * Apply the Function by adapting the input and outputs.
     *
     * @param input Input to adapt and apply function to
     * @return Adapted output
     */
    @Override
    public IO apply(final IO input, final User user, final Justification justification) {
        return adaptOutput(rule.apply(adaptInput(input), user, justification), input);
    }

    public Rule<T> getRule() {
        return rule;
    }

    public void setRule(final Rule<T> rule) {
        this.rule = rule;
    }
}
