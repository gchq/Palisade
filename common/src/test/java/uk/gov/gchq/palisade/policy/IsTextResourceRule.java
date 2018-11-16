package uk.gov.gchq.palisade.policy;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.rule.Rule;

import java.io.Serializable;

public class IsTextResourceRule implements Serializable, Rule<Resource> {
    @Override
    public Resource apply(final Resource record, final User user, final Context context) {
        if (record instanceof LeafResource) {
            if (((LeafResource) record).getSerialisedFormat().equalsIgnoreCase("txt")) {
                return record;
            }
        }
        return null;
    }
}
