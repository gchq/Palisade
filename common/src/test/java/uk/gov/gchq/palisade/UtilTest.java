package uk.gov.gchq.palisade;

import com.google.common.collect.Lists;
import org.junit.Test;

import uk.gov.gchq.palisade.policy.Rules;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.gchq.palisade.Util.applyRules;

public class UtilTest {

    @Test
    public void shouldReturnResourceIfNoRules() throws Exception {
        //when
        final String actual1 = applyRules("String", null, null, null);
        final String actual2 = applyRules("String", null, null, new Rules<>());
        //then
        assertEquals("String", actual1);
        assertEquals("String", actual2);
    }

    @Test
    public void shouldUpdateRecord() throws Exception {
        //given
        final Rules<String> rules = new Rules<String>().rule("r1", (record, user, justification) -> "fromRule");
        //when
        final String actual1 = applyRules("String", null, null, rules);
        assertEquals("fromRule", actual1);
    }

    @Test
    public void shouldUpdateRecordFromAllRules() throws Exception {
        //given
        final Rules<String> rules = new Rules<String>()
                .rule("r1", (record, user, justification) -> "fromRule")
                .rule("r2", (record, user, justification) -> record.concat("2ndRule"));
        //when
        final String actual1 = applyRules("String", null, null, rules);
        //then
        assertEquals("fromRule" + "2ndRule", actual1);
    }


    @Test
    public void shouldUpdateStreamOfRecordsFromAllRules() throws Exception {
        //given
        final Stream<String> stream = Lists.newArrayList("one", "two").stream();
        final Rules<String> rules = new Rules<String>()
                .rule("r2", (record, user, justification) -> record.concat("2ndRule"))
                .rule("r3", (record, user, justification) -> record.concat("3rdRule"));
        //when
        final List<String> result = applyRules(stream, null, null, rules).collect(Collectors.toList());
        //then
        assertTrue(result.stream().filter(s -> s.equals("one" + "2ndRule" + "3rdRule")).findAny().isPresent());
        assertTrue(result.stream().filter(s -> s.equals("two" + "2ndRule" + "3rdRule")).findAny().isPresent());

    }
}