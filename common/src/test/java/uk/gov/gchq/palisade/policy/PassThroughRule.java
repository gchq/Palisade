package uk.gov.gchq.palisade.policy;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.rule.Rule;

import java.io.Serializable;

public class PassThroughRule<T> implements Serializable, Rule<T> {
    @Override
    public T apply(final T record, final User user, final Context context) {
        return record;
    }
}
