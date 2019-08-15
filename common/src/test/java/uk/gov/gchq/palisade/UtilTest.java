package uk.gov.gchq.palisade;

import com.google.common.collect.Lists;
import org.junit.Test;

import uk.gov.gchq.palisade.rule.Rules;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.gchq.palisade.Util.applyRulesToItem;

public class UtilTest {

    @Test
    public void shouldReturnResourceIfNoRules() throws Exception {
        //when
        final AtomicLong recordsProcessed = new AtomicLong(0);
        final AtomicLong recordsReturned = new AtomicLong(0);
        final String actual1 = applyRulesToItem("String", null, null, null, recordsProcessed, recordsReturned);
        final String actual2 = applyRulesToItem("String", null, null, new Rules<>(), recordsProcessed, recordsReturned);
        //then
        assertEquals("String", actual1);
        assertEquals("String", actual2);
        assertEquals(2, recordsProcessed.get());
        assertEquals(2, recordsReturned.get());
    }

    @Test
    public void shouldUpdateRecord() throws Exception {
        //given
        final AtomicLong recordsProcessed = new AtomicLong(0);
        final AtomicLong recordsReturned = new AtomicLong(0);
        final Rules<String> rules = new Rules<String>().rule("r1", (record, user, context) -> "fromRule");
        //when
        final String actual1 = applyRulesToItem("String", null, null, rules, recordsProcessed, recordsReturned);
        assertEquals("fromRule", actual1);
        assertEquals(1, recordsProcessed.get());
        assertEquals(1, recordsReturned.get());
    }

    @Test
    public void shouldUpdateRecordFromAllRules() throws Exception {
        //given
        final AtomicLong recordsProcessed = new AtomicLong(0);
        final AtomicLong recordsReturned = new AtomicLong(0);
        final Rules<String> rules = new Rules<String>()
                .rule("r1", (record, user, context) -> "fromRule")
                .rule("r2", (record, user, context) -> record.concat("2ndRule"));
        //when
        final String actual1 = applyRulesToItem("String", null, null, rules, recordsProcessed, recordsReturned);
        //then
        assertEquals("fromRule" + "2ndRule", actual1);
        assertEquals(1, recordsProcessed.get());
        assertEquals(1, recordsReturned.get());
    }

    @Test
    public void shouldUpdateStreamOfRecordsFromAllRules() throws Exception {
        //given
        final AtomicLong recordsProcessed = new AtomicLong(0);
        final AtomicLong recordsReturned = new AtomicLong(0);
        final Stream<String> stream = Lists.newArrayList("one", "two").stream();
        final Rules<String> rules = new Rules<String>()
                .rule("r2", (record, user, context) -> record.concat("2ndRule"))
                .rule("r3", (record, user, context) -> record.concat("3rdRule"));
        //when
        final List<String> result = Util.applyRulesToStream(stream, null, null, rules, recordsProcessed, recordsReturned).collect(Collectors.toList());
        //then
        assertTrue(result.stream().filter(s -> s.equals("one" + "2ndRule" + "3rdRule")).findAny().isPresent());
        assertTrue(result.stream().filter(s -> s.equals("two" + "2ndRule" + "3rdRule")).findAny().isPresent());
        assertEquals(2, recordsProcessed.get());
        assertEquals(2, recordsReturned.get());
    }
}
