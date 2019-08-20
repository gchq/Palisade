package uk.gov.gchq.palisade.policy.tuple;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.palisade.Context;
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
    public static final String USER_ROLES = "User.roles";
    public static final String CONTEXT_PURPOSE = "Context.purpose";
    public static final String AUTH_1 = "auth1";
    public static final String PURPOSE_1 = "purpose1";
    public static final String FROM_RULE = "fromRule";
    public static final String NOT_VAL1 = "notVal1";
    public static final String OTHER = "other";
    public static final String ROLE_1 = "role1";
    private static final User NULL_USER = new User().userId("user1");
    private static final Context NULL_CONTEXT = new Context().purpose("testing");

    private TupleRule<TestObj> testObject;
    private TestObj record;

    @Before
    public void setUp() throws Exception {
        record = new TestObj();
    }

    @Test
    public void shouldSelectAndMatchRecord() throws Exception {
        //given
        testObject = new TupleRule<TestObj>()
                .selection(RECORD_VAR1)
                .predicate((String o) -> o.equals(TestObj.VAL_1));

        //when
        final TestObj actual = testObject.apply(record, NULL_USER, NULL_CONTEXT);

        //then
        assertEquals(record, actual);
    }

    @Test
    public void shouldSelectAndNotMatchRecord() throws Exception {
        //given
        testObject = new TupleRule<TestObj>()
                .selection(RECORD_VAR1)
                .predicate((String o) -> o.equals(TestObj.VAL_1));
        record.var1 = NOT_VAL1;

        //when
        final TestObj object = testObject.apply(record, NULL_USER, NULL_CONTEXT);

        //then
        Assert.assertNull(object);
    }

    @Test
    public void shouldSelectAndProjectRecord() throws Exception {
        //given
        testObject = new TupleRule<TestObj>()
                .selection(RECORD_VAR1)
                .function((String s) -> FROM_RULE)
                .projection(RECORD_VAR2);
        final String varStart = record.var2;
        //when
        final String actual = testObject.apply(record, NULL_USER, NULL_CONTEXT).getVar2();
        //then
        assertNull(varStart);
        assertEquals(FROM_RULE, actual);
        assertEquals(record.var2, actual);
    }

    @Test
    public void shouldSelectAndMatchUser() throws Exception {
        //given
        final User user = new User().auths(AUTH_1);
        testObject = new TupleRule<TestObj>()
                .selection(USER_AUTHS)
                .predicate((Set<String> o) -> o.contains(AUTH_1));

        //when
        final TestObj actual = testObject.apply(record, user, NULL_CONTEXT);

        //then
        assertEquals(record, actual);
    }

    @Test
    public void shouldSelectAndNotMatchUser() throws Exception {
        //given
        final User user = new User();
        testObject = new TupleRule<TestObj>()
                .selection(USER_AUTHS)
                .predicate((Set<String> o) -> o.contains(AUTH_1));

        //when
        final TestObj object = testObject.apply(record, user, NULL_CONTEXT);
        //then
        assertNull(object);
    }

    @Test
    public void shouldSelectUserAndProjectRecord() throws Exception {
        //given
        final User user = new User().auths(AUTH_1);
        testObject = new TupleRule<TestObj>()
                .selection(USER_AUTHS)
                .function((Set<String> o) -> FROM_RULE)
                .projection(RECORD_VAR2);
        final String var2Start = record.var2;
        //when
        final String actual = testObject.apply(record, user, NULL_CONTEXT).getVar2();
        //then
        assertNull(var2Start);
        assertEquals(FROM_RULE, actual);
        assertEquals(record.var2, actual);
    }

    @Test
    public void shouldSelectAndProjectUser() throws Exception {
        //given
        final User user = new User().auths(AUTH_1);
        testObject = new TupleRule<TestObj>()
                .selection(USER_AUTHS)
                .function((Set<String> o) -> Sets.newHashSet(FROM_RULE));
        final Set<String> authsStart = user.getAuths();
        //when
        final TestObj apply = testObject.apply(record, user, NULL_CONTEXT);

        //then
        assertEquals(record, apply);
        assertEquals(1, user.getAuths().size());
        assertFalse(user.getAuths().contains(AUTH_1));
        assertTrue(user.getAuths().contains(FROM_RULE));
        assertFalse(authsStart.contains(FROM_RULE));
    }

    @Test
    public void shouldSelectAndProjectUserRoles() throws Exception {
        //given
        final User user = new User().roles(ROLE_1);
        testObject = new TupleRule<TestObj>()
                .selection(USER_ROLES)
                .function((Set<String> o) -> Sets.newHashSet(FROM_RULE));
        final Set<String> rolesStart = user.getRoles();
        //when
        final TestObj apply = testObject.apply(record, user, NULL_CONTEXT);

        //then
        assertEquals(record, apply);
        assertEquals(1, user.getRoles().size());
        assertFalse(user.getRoles().contains(ROLE_1));
        assertTrue(user.getRoles().contains(FROM_RULE));
        assertFalse(rolesStart.contains(FROM_RULE));
    }

    @Test
    public void shouldSelectAndMatchPurpose() throws Exception {
        //given
        final Context purpose = new Context().purpose(PURPOSE_1);
        testObject = new TupleRule<TestObj>()
                .selection(CONTEXT_PURPOSE)
                .predicate((String s) -> s.equals(PURPOSE_1));

        //when
        final TestObj actual = testObject.apply(record, NULL_USER, purpose);
        //then
        assertEquals(record, actual);
    }

    @Test
    public void shouldSelectAndNotMatchPurpose() throws Exception {
        //given
        final Context purpose = new Context().purpose(PURPOSE_1);
        testObject = new TupleRule<TestObj>()
                .selection(CONTEXT_PURPOSE)
                .predicate((String s) -> s.equals(OTHER));

        //when
        final TestObj actual2 = testObject.apply(record, NULL_USER, purpose);
        //then
        assertEquals(null, actual2);
    }

    @Test
    public void shouldSelectAndProjectPurpose() throws Exception {
        //given
        final Context purpose = new Context().purpose(PURPOSE_1);
        testObject = new TupleRule<TestObj>()
                .selection(CONTEXT_PURPOSE)
                .function((String o) -> FROM_RULE)
                .projection(CONTEXT_PURPOSE);
        //when
        final TestObj actual = testObject.apply(record, NULL_USER, purpose);
        //then
        assertEquals(record, actual);
        assertEquals(FROM_RULE, purpose.getPurpose());
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
