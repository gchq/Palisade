package uk.gov.gchq.palisade.audit.service.impl;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestReceivedAuditRequest;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.user.service.UserService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StroomAuditServiceTest {

    private static final PrintStream sysOut = System.out;
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream newSysOut = new PrintStream(outContent);

    @BeforeClass
    public static void before() {
        System.setOut(newSysOut);
    }

    @AfterClass
    public static void after() {
        System.setOut(sysOut);
    }

    @Test
    public void auditRegisterRequestReceived() {
        // Given
        //this test needs to be rewritten to use slf4j - and interept the loggerFactory for event-logger
        //top level pom.xml includes log4j - log4j is deprecated by slf4j.
        //The spring boot changes will convert all calls to log4j into slf4j

        final String resourceId = "a pointer to a data resource";
        final String userId = "an identifier for the user";
        final String purpose = "the purpose for the data access request";
        final String exceptionOriginalRequestId = "originalRequestId linking all logs from the same data access request together";

        final RegisterRequestReceivedAuditRequest auditRequestReceived = new RegisterRequestReceivedAuditRequest();
        UserId mockUserId = Mockito.mock(UserId.class);
        Mockito.doReturn(userId).when(mockUserId).getId();
        Context mockContext = Mockito.mock(Context.class);
        Mockito.doReturn(purpose).when(mockContext).getPurpose();
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(exceptionOriginalRequestId).when(mockRequestId).getId();
        auditRequestReceived
                .resourceId(resourceId)
                .userId(mockUserId)
                .context(mockContext)
                .originalRequestId(mockRequestId);

        // When
        StroomAuditService stroomAuditService = new StroomAuditService();
        stroomAuditService.audit(auditRequestReceived);

        //Then
        final String log = outContent.toString();
        verify(mockUserId, Mockito.atLeastOnce()).getId();
        verify(mockContext, Mockito.atLeastOnce()).getPurpose();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(resourceId));
        Assert.assertTrue(log.contains(userId));
        Assert.assertTrue(log.contains(purpose));
        Assert.assertTrue(log.contains(exceptionOriginalRequestId));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_RECEIVED_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestCompleted() {
        // Given
        //this test needs to be rewritten to use slf4j - and interept the loggerFactory for event-logger
        //top level pom.xml includes log4j - log4j is deprecated by slf4j.
        //The spring boot changes will convert all calls to log4j into slf4j

        final String resourceId = "a pointer to a data resource";
        final String userId = "an identifier for the user";
        final String exceptionOriginalRequestId = "originalRequestId linking all logs from the same data access request together";
        final String resourceSerialisedFormat = "serialised format of the resource";
        final String resourceDataType = "data type of the resource";

        final RegisterRequestCompleteAuditRequest auditRequestComplete = new RegisterRequestCompleteAuditRequest();
        UserId mockUserId = Mockito.mock(UserId.class);
        User mockUser = Mockito.mock(User.class);
        Mockito.doReturn(userId).when(mockUserId).getId();
        Mockito.doReturn(mockUserId).when(mockUser).getUserId();
        Set<LeafResource> resources = Sets.newSet(new FileResource().id(resourceId).serialisedFormat(resourceSerialisedFormat).type(resourceDataType));
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(exceptionOriginalRequestId).when(mockRequestId).getId();
        auditRequestComplete
                .user(mockUser)
                .leafResources(resources)
                .originalRequestId(mockRequestId);

        // When
        StroomAuditService stroomAuditService = new StroomAuditService();
        stroomAuditService.audit(auditRequestComplete);

        //Then
        final String log = outContent.toString();
        verify(mockUserId, Mockito.atLeastOnce()).getId();
        verify(mockUser, Mockito.atLeastOnce()).getUserId();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(resourceId));
        Assert.assertTrue(log.contains(userId));
        Assert.assertTrue(log.contains(exceptionOriginalRequestId));
        Assert.assertTrue(log.contains(resourceDataType));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_AUTHENTICATION_SUCCESS_DESCRIPTION));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_COMPLETED_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestExceptionFromPolicyService() {
        // Given
        //this test needs to be rewritten to use slf4j - and interept the loggerFactory for event-logger
        //top level pom.xml includes log4j - log4j is deprecated by slf4j.
        //The spring boot changes will convert all calls to log4j into slf4j

        final String exceptionMessage = "exception message";
        final String exceptionOriginalRequestId = "originalRequestId linking all logs from the same data access request together";

        final RegisterRequestExceptionAuditRequest auditRequestWithException = new RegisterRequestExceptionAuditRequest();
        Throwable mockException = Mockito.mock(Throwable.class);
        Mockito.doReturn(exceptionMessage).when(mockException).getMessage();
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(exceptionOriginalRequestId).when(mockRequestId).getId();
        auditRequestWithException
                .exception(mockException)
                .service(PolicyService.class)
                .originalRequestId(mockRequestId);

        // When
        StroomAuditService stroomAuditService = new StroomAuditService();
        stroomAuditService.audit(auditRequestWithException);

        //Then
        final String log = outContent.toString();
        verify(mockException, Mockito.atLeastOnce()).getMessage();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(exceptionMessage));
        Assert.assertTrue(log.contains(exceptionOriginalRequestId));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestExceptionFromUserService() {
        // Given
        //this test needs to be rewritten to use slf4j - and interept the loggerFactory for event-logger
        //top level pom.xml includes log4j - log4j is deprecated by slf4j.
        //The spring boot changes will convert all calls to log4j into slf4j

        final String exceptionMessage = "exception message";
        final String exceptionOriginalRequestId = "originalRequestId linking all logs from the same data access request together";

        final RegisterRequestExceptionAuditRequest auditRequestWithException = new RegisterRequestExceptionAuditRequest();
        Throwable mockException = Mockito.mock(Throwable.class);
        Mockito.doReturn(exceptionMessage).when(mockException).getMessage();
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(exceptionOriginalRequestId).when(mockRequestId).getId();
        auditRequestWithException
                .exception(mockException)
                .service(UserService.class)
                .originalRequestId(mockRequestId);

        // When
        StroomAuditService stroomAuditService = new StroomAuditService();
        stroomAuditService.audit(auditRequestWithException);

        //Then
        final String log = outContent.toString();
        verify(mockException, Mockito.atLeastOnce()).getMessage();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(exceptionMessage));
        Assert.assertTrue(log.contains(exceptionOriginalRequestId));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_USER_EXCEPTION_DESCRIPTION));
    }
}