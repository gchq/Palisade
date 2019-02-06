package uk.gov.gchq.palisade.example.rule;

import org.junit.Test;
import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ZipCodeMaskingTest {
    private static final Employee TEST_EMPLOYEE = Employee.generate(new Random(1));
    private static final User TEST_USER_NOT_ESTATES = new User().roles("Not Estates"); // Role not in HR
    private static final User TEST_USER_ESTATES = new User().roles("Estates"); // Role is HR


    private static final ZipCodeMaskingRule ZIP_CODE_MASKING_RULE = new ZipCodeMaskingRule();
    private static final Context STAFF_REPORT_CONTEXT = new Context().justification("");

    @Test
    public void shouldRedactForEstate() {
        // Given - Employee, Role, Reason

        // When
        Employee actual = ZIP_CODE_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_ESTATES, STAFF_REPORT_CONTEXT);
        String actualZipCode = actual.getAddress().getZipCode();
        String actualStreetNum = actual.getAddress().getStreetAddressNumber();
        String actualStreetName = actual.getAddress().getStreetName();
        String testZipCode = TEST_EMPLOYEE.getAddress().getZipCode();

        // Then
        assertEquals(testZipCode.substring(0,testZipCode.length() - 1), actualZipCode.substring(0,testZipCode.length() - 1));
        assertEquals(testZipCode.charAt(testZipCode.length() - 1), actualZipCode.charAt(actualZipCode.length() - 1));
        assertNull(actualStreetNum);
        assertNull(actualStreetName);

    }

    @Test
    public void shouldNotRedactForNotEstate() {
        // Given - Employee, Role, Reason

        // When
        Employee actual = ZIP_CODE_MASKING_RULE.apply(TEST_EMPLOYEE, TEST_USER_NOT_ESTATES, STAFF_REPORT_CONTEXT);


        // Then
        assertEquals(TEST_EMPLOYEE, actual);

    }
}