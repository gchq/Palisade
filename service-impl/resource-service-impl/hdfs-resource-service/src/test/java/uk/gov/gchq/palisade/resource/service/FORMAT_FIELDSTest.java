package uk.gov.gchq.palisade.resource.service;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import uk.gov.gchq.palisade.resource.service.HDFSResourceService.FORMAT_FIELDS;

import java.util.HashSet;

public class FORMAT_FIELDSTest {

    @Test
    public void shouldNotUseDuplicateFields() throws Exception {
        final HashSet<Integer> index = Sets.newHashSet();
        for (FORMAT_FIELDS field : FORMAT_FIELDS.values()) {
            if (!index.add(field.pos())) {
                Assert.fail("duplicate positions used for field: " + field);
            }
        }
    }
}