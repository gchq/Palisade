package uk.gov.gchq.palisade.policy.tuple;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.User;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TupleRuleTest {

    public static final String RECORD_VAR1 = "Record.var1";
    public static final String RECORD_VAR2 = "Record.var2";
    public static final String USER_AUTHS = "User.auths";
    public static final String JUSTIFICATION_JUSTIFICATION = "Justification.justification";
    public static final String AUTH_1 = "auth1";
    public static final String JUST_1 = "just1";
    public static final String FROM_RULE = "fromRule";
    public static final String NOT_VAL1 = "notVal1";
    public static final String OTHER = "other";
    private TupleRule<TestObj> testObject;
    private TestObj record;

    @Before
    public void setUp() throws Exception {
        record = new TestObj();
    }

    @Test
    public void shouldSelectAndMatchRecord() throws Exception {
        //given
        testObject = new TupleRule<>(RECORD_VAR1, (String o) -> {
            return o.equals(TestObj.VAL_1);
        });

        //when
        final TestObj actual = testObject.apply(record, null, null);

        //then
        assertEquals(record, actual);
    }

    @Test
    public void shouldSelectAndNotMatchRecord() throws Exception {
        //given
        testObject = new TupleRule<>(RECORD_VAR1, (String o) -> {
            return o.equals(TestObj.VAL_1);
        });
        record.var1 = NOT_VAL1;

        //when
        final TestObj object = testObject.apply(record, null, null);

        //then
        Assert.assertNull(object);
    }

    @Test
    public void shouldSelectAndProjectRecord() throws Exception {
        //given
        testObject = new TupleRule<>(RECORD_VAR1, (String s) -> FROM_RULE, RECORD_VAR2);
        final String varStart = record.var2;
        //when
        final String actual = testObject.apply(record, null, null).getVar2();
        //then
        assertNull(varStart);
        assertEquals(FROM_RULE, actual);
        assertEquals(record.var2, actual);
    }

    @Test
    public void shouldSelectAndMatchUser() throws Exception {
        //given
        final User user = new User().auths(AUTH_1);
        testObject = new TupleRule<>(USER_AUTHS, (Set<String> o) -> {
            return o.contains(AUTH_1);
        });

        //when
        final TestObj actual = testObject.apply(record, user, null);

        //then
        assertEquals(record, actual);
    }

    @Test
    public void shouldSelectAndNotMatchUser() throws Exception {
        //given
        final User user = new User();
        testObject = new TupleRule<>(USER_AUTHS, (Set<String> o) -> {
            return o.contains(AUTH_1);
        });

        //when
        final TestObj object = testObject.apply(record, user, null);
        //then
        assertNull(object);
    }

    @Test
    public void shouldSelectUserAndProjectRecord() throws Exception {
        //given
        final User user = new User().auths(AUTH_1);
        testObject = new TupleRule<>(USER_AUTHS, (Set<String> o) -> FROM_RULE, RECORD_VAR2);
        final String var2Start = record.var2;
        //when
        final String actual = testObject.apply(record, user, null).getVar2();
        //then
        assertNull(var2Start);
        assertEquals(FROM_RULE, actual);
        assertEquals(record.var2, actual);
    }

    @Test
    public void shouldSelectAndProjectUser() throws Exception {
        //given
        final User user = new User().auths(AUTH_1);
        testObject = new TupleRule<>(USER_AUTHS, (Set<String> o) -> {
            return Sets.newHashSet(FROM_RULE);
        }, USER_AUTHS);
        final Set<String> authsStart = user.getAuths();
        //when
        final TestObj apply = testObject.apply(record, user, null);

        //then
        assertEquals(record, apply);
        assertEquals(1, user.getAuths().size());
        assertFalse(user.getAuths().contains(AUTH_1));
        assertTrue(user.getAuths().contains(FROM_RULE));
        assertFalse(authsStart.contains(FROM_RULE));
    }

    @Test
    public void shouldSelectAndMatchJust() throws Exception {
        //given
        final Justification just = new Justification(JUST_1);
        testObject = new TupleRule<>(JUSTIFICATION_JUSTIFICATION, (String s) -> {
            return s.equals(JUST_1);
        });

        //when
        final TestObj actual = testObject.apply(record, null, just);
        //then
        assertEquals(record, actual);
    }

    @Test
    public void shouldSelectAndNotMatchJust() throws Exception {
        //given
        final Justification just = new Justification(JUST_1);
        testObject = new TupleRule<>(JUSTIFICATION_JUSTIFICATION, (String s) -> {
            return s.equals(OTHER);
        });

        //when
        final TestObj actual2 = testObject.apply(record, null, just);
        //then
        assertEquals(null, actual2);
    }

    @Test
    public void shouldSelectAndProjectJust() throws Exception {
        //given
        final Justification just = new Justification(JUST_1);
        testObject = new TupleRule<>(JUSTIFICATION_JUSTIFICATION, (String o) -> FROM_RULE, JUSTIFICATION_JUSTIFICATION);
        //when
        final TestObj actual = testObject.apply(record, null, just);
        //then
        assertEquals(record, actual);
        assertEquals(FROM_RULE, just.getJustification());
    }


    public static class TestObj {
        public static final String VAL_1 = "val1";
        public String var1 = VAL_1;
        public String var2;

        public String getVar1() {
            return var1;
        }

        public String getVar2() {
            return var2;
        }
    }
}
