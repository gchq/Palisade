package uk.gov.gchq.palisade.audit.service.impl;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestReceivedAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadResponseAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestReceivedAuditRequest;
import uk.gov.gchq.palisade.policy.service.PolicyService;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.user.service.UserService;

import java.util.Set;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StroomAuditServiceTest {

    private static final StroomAuditService stroomAuditService= createStroomAuditService();

    @Mock
    AppenderSkeleton appender;
    @Captor
    ArgumentCaptor<LoggingEvent> logCaptor;

    private static StroomAuditService createStroomAuditService() {
        return new StroomAuditService()
                .organisation("Test Org")
                .systemClassification("Some system classification")
                .systemDescription("some system description")
                .systemEnv("some system env")
                .systemName("some system name")
                .systemVersion("some system version");
    }

    @Test
    public void auditRegisterRequestReceived() {
        // Given
        Logger.getRootLogger().addAppender(appender);

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
        stroomAuditService.audit(auditRequestReceived);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
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
        Logger.getRootLogger().addAppender(appender);

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
        stroomAuditService.audit(auditRequestComplete);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String authenticationLog = logCaptor.getAllValues().get(1).getRenderedMessage();
        final String registerSuccessLog = logCaptor.getAllValues().get(2).getRenderedMessage();
        verify(mockUserId, Mockito.atLeastOnce()).getId();
        verify(mockUser, Mockito.atLeastOnce()).getUserId();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(authenticationLog.contains(userId));
        Assert.assertTrue(authenticationLog.contains(exceptionOriginalRequestId));
        Assert.assertTrue(authenticationLog.contains(StroomAuditService.REGISTER_REQUEST_AUTHENTICATION_SUCCESS_DESCRIPTION));
        Assert.assertTrue(registerSuccessLog.contains(resourceId));
        Assert.assertTrue(registerSuccessLog.contains(exceptionOriginalRequestId));
        Assert.assertTrue(registerSuccessLog.contains(resourceDataType));
        Assert.assertTrue(registerSuccessLog.contains(StroomAuditService.REGISTER_REQUEST_COMPLETED_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestExceptionFromPolicyService() {
        // Given
        Logger.getRootLogger().addAppender(appender);

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
        stroomAuditService.audit(auditRequestWithException);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockException, Mockito.atLeastOnce()).getMessage();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(exceptionMessage));
        Assert.assertTrue(log.contains(exceptionOriginalRequestId));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestExceptionFromUserService() {
        // Given
        Logger.getRootLogger().addAppender(appender);

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
        stroomAuditService.audit(auditRequestWithException);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockException, Mockito.atLeastOnce()).getMessage();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(exceptionMessage));
        Assert.assertTrue(log.contains(exceptionOriginalRequestId));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_USER_EXCEPTION_DESCRIPTION));
    }

    @Test
    public void auditReadRequestReceived() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        final String resourceType = "Resource type, e.g. Employee";
        final String resourceId = "resource id";
        final String originalRequestId = "originalRequestId linking all logs from the same data access request together";

        final ReadRequestReceivedAuditRequest auditReadRequestReceived = new ReadRequestReceivedAuditRequest();
        FileResource mockResource = Mockito.mock(FileResource.class);
        Mockito.doReturn(resourceType).when(mockResource).getType();
        Mockito.doReturn(resourceId).when(mockResource).getId();
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(originalRequestId).when(mockRequestId).getId();
        auditReadRequestReceived
                .resource(mockResource)
                .originalRequestId(mockRequestId);

        // When
        stroomAuditService.audit(auditReadRequestReceived);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockResource, Mockito.atLeastOnce()).getType();
        verify(mockResource, Mockito.atLeastOnce()).getId();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(resourceId));
        Assert.assertTrue(log.contains(resourceType));
        Assert.assertTrue(log.contains(originalRequestId));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_RECEIVED_DESCRIPTION));
    }

    @Test
    public void auditReadRequestComplete() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        final String resourceId = "resource id";
        final long numberOfRecordsProcessed = Long.MAX_VALUE;
        final long numberOfRecordsReturned = 5;
        final String originalRequestId = "originalRequestId linking all logs from the same data access request together";

        final ReadRequestCompleteAuditRequest auditReadRequestComplete = new ReadRequestCompleteAuditRequest();
        FileResource mockResource = Mockito.mock(FileResource.class);
        Mockito.doReturn(resourceId).when(mockResource).getId();
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(originalRequestId).when(mockRequestId).getId();
        auditReadRequestComplete
                .resource(mockResource)
                .numberOfRecordsProcessed(numberOfRecordsProcessed)
                .numberOfRecordsReturned(numberOfRecordsReturned)
                .originalRequestId(mockRequestId);

        // When
        stroomAuditService.audit(auditReadRequestComplete);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockResource, Mockito.atLeastOnce()).getId();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(resourceId));
        // Stroom doesn't currently have the functionalility to state the number of records processed
//        Assert.assertTrue(log.contains(String.valueOf(numberOfRecordsProcessed)));
        Assert.assertTrue(log.contains(String.valueOf(numberOfRecordsReturned)));
        Assert.assertTrue(log.contains(originalRequestId));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_COMPLETED_DESCRIPTION));
    }

    @Test
    public void auditReadRequestException() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        final String resourceId = "resource id";
        final String token = "token in the form of a UUID";
        final String exceptionErrorMessage = "The error message from the exception thrown";
        final String originalRequestId = "originalRequestId linking all logs from the same data access request together";

        final ReadRequestExceptionAuditRequest auditReadRequestException = new ReadRequestExceptionAuditRequest();
        FileResource mockResource = Mockito.mock(FileResource.class);
        Mockito.doReturn(resourceId).when(mockResource).getId();
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(originalRequestId).when(mockRequestId).getId();
        auditReadRequestException
                .resource(mockResource)
                .token(token)
                .exception(new Throwable(exceptionErrorMessage))
                .originalRequestId(mockRequestId);

        // When
        stroomAuditService.audit(auditReadRequestException);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockResource, Mockito.atLeastOnce()).getId();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        Assert.assertTrue(log.contains(resourceId));
        Assert.assertTrue(log.contains(originalRequestId));
        Assert.assertTrue(log.contains(token));
        Assert.assertTrue(log.contains(exceptionErrorMessage));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_EXCEPTION_DESCRIPTION));
    }

    @Test
    public void auditReadResponse() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        final String resourceId = "resource id";
        final String rulesAppliedMessage = "human readable description of the rules/policies been applied to the data";
        final String originalRequestId = "originalRequestId linking all logs from the same data access request together";

        final ReadResponseAuditRequest auditReadResponse = new ReadResponseAuditRequest();
        FileResource mockResource = Mockito.mock(FileResource.class);
        Mockito.doReturn(resourceId).when(mockResource).getId();
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(originalRequestId).when(mockRequestId).getId();
        Rules mockRules = Mockito.mock(Rules.class);
        Mockito.doReturn(rulesAppliedMessage).when(mockRules).getMessage();
        auditReadResponse
                .resource(mockResource)
                .rulesApplied(mockRules)
                .originalRequestId(mockRequestId);

        // When
        stroomAuditService.audit(auditReadResponse);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockResource, Mockito.atLeastOnce()).getId();
        verify(mockRequestId, Mockito.atLeastOnce()).getId();
        verify(mockRules, Mockito.atLeastOnce()).getMessage();
        Assert.assertTrue(log.contains(resourceId));
        Assert.assertTrue(log.contains(originalRequestId));
        Assert.assertTrue(log.contains(rulesAppliedMessage));
        Assert.assertTrue(log.contains(StroomAuditService.READ_RESPONSE_DESCRIPTION));
    }
}