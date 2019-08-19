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
import uk.gov.gchq.palisade.audit.service.request.AuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.ReadRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.service.ResourceService;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.user.service.UserService;

import java.util.HashSet;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StroomAuditServiceTest {

    private static final StroomAuditService STROOM_AUDIT_SERVICE= createStroomAuditService();
    private static final String TEST_USER_ID = "an identifier for the user";
    private static final String TEST_RESOURCE_ID = "a pointer to a data resource";
    private static final String TEST_PURPOSE = "the purpose for the data access request";
    private static final String TEST_ORIGINAL_REQUEST_ID = "originalRequestId linking all logs from the same data access request together";
    private static final String TEST_SERIALISED_FORMAT = "serialised format of the resource, e.g. Avro, CSV";
    private static final String TEST_DATA_TYPE = "data type of the resource, e.g. Employee";
    private static final String TEST_EXCEPTION_MESSAGE = "exception message";
    private static final long TEST_NUMBER_OF_RECORDS_PROCESSED = 20;
    private static final long TEST_NUMBER_OF_RECORDS_RETURNED = 5;
    private static final String TEST_TOKEN = "token in the form of a UUID";
    private static final String TEST_RULES_APPLIED = "human readable description of the rules/policies been applied to the data";

    private static StroomAuditService createStroomAuditService() {
        return new StroomAuditService()
                .organisation("Test Org")
                .systemClassification("Some system classification")
                .systemDescription("some system description")
                .systemEnv("some system env")
                .systemName("some system name")
                .systemVersion("some system version");
    }

    private UserId mockUserID() {
        final UserId mockUserId = Mockito.mock(UserId.class);
        Mockito.doReturn(TEST_USER_ID).when(mockUserId).getId();
        return mockUserId;
    }

    private User mockUser() {
        final User mockUser = Mockito.mock(User.class);
        Mockito.doReturn(mockUserID()).when(mockUser).getUserId();
        return mockUser;
    }

    private Context mockContext() {
        final Context mockContext = Mockito.mock(Context.class);
        Mockito.doReturn(TEST_PURPOSE).when(mockContext).getPurpose();
        return mockContext;
    }

    private RequestId mockOriginalRequestId() {
        final RequestId mockOriginalRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn(TEST_ORIGINAL_REQUEST_ID).when(mockOriginalRequestId).getId();
        return mockOriginalRequestId;
    }

    private LeafResource mockResource() {
        final LeafResource mockResource = Mockito.mock(LeafResource.class);
        Mockito.doReturn(TEST_RESOURCE_ID).when(mockResource).getId();
        Mockito.doReturn(TEST_DATA_TYPE).when(mockResource).getType();
        Mockito.doReturn(TEST_SERIALISED_FORMAT).when(mockResource).getSerialisedFormat();
        return mockResource;
    }

    private Exception mockException() {
        final Exception mockException = Mockito.mock(Exception.class);
        Mockito.doReturn(TEST_EXCEPTION_MESSAGE).when(mockException).getMessage();
        return mockException;
    }

    private Rules mockRules() {
        final Rules mockRules = Mockito.mock(Rules.class);
        Mockito.doReturn(TEST_RULES_APPLIED).when(mockRules).getMessage();
        return mockRules;
    }


    @Mock
    AppenderSkeleton appender;
    @Captor
    ArgumentCaptor<LoggingEvent> logCaptor;

    @Test
    public void auditRegisterRequestWithNoResources() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        // mock user
        final User mockUser = mockUser();
        // mock context
        final Context mockContext = mockContext();
        // mock original request id
        final RequestId mockOriginalRequestId = mockOriginalRequestId();

        final AuditRequest auditRequest = new RegisterRequestCompleteAuditRequest()
                .user(mockUser)
                .context(mockContext)
                .leafResources(new HashSet<>(0));
        auditRequest.setOriginalRequestId(mockOriginalRequestId);

        // When
        STROOM_AUDIT_SERVICE.audit(auditRequest);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockOriginalRequestId, Mockito.atLeastOnce()).getId();
        verify(mockUser, Mockito.atLeastOnce()).getUserId();
        verify(mockContext, Mockito.atLeastOnce()).getPurpose();
        Assert.assertTrue(log.contains(TEST_USER_ID));
        Assert.assertTrue(log.contains(TEST_PURPOSE));
        Assert.assertTrue(log.contains(TEST_ORIGINAL_REQUEST_ID));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_NO_RESOURCES_TYPE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_NO_RESOURCES_DESCRIPTION));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_NO_RESOURCES_OUTCOME_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestSuccessful() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        // mock user
        final User mockUser = mockUser();
        // mock context
        final Context mockContext = mockContext();
        // mock original request id
        final RequestId mockOriginalRequestId = mockOriginalRequestId();
        // mock resource
        final LeafResource mockResource = mockResource();

        final AuditRequest auditRequest = new RegisterRequestCompleteAuditRequest()
                .user(mockUser)
                .context(mockContext)
                .leafResources(Sets.newSet(mockResource));
        auditRequest.setOriginalRequestId(mockOriginalRequestId);

        // When
        STROOM_AUDIT_SERVICE.audit(auditRequest);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockOriginalRequestId, Mockito.atLeastOnce()).getId();
        verify(mockUser, Mockito.atLeastOnce()).getUserId();
        verify(mockContext, Mockito.atLeastOnce()).getPurpose();
        verify(mockResource, Mockito.atLeastOnce()).getId();
        verify(mockResource, Mockito.atLeastOnce()).getType();
        Assert.assertTrue(log.contains(TEST_USER_ID));
        Assert.assertTrue(log.contains(TEST_PURPOSE));
        Assert.assertTrue(log.contains(TEST_ORIGINAL_REQUEST_ID));
        Assert.assertTrue(log.contains(TEST_RESOURCE_ID));
        Assert.assertTrue(log.contains(TEST_DATA_TYPE));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_COMPLETED_TYPE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_COMPLETED_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestUserException() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        // mock userId
        final UserId mockUserId = mockUserID();
        // mock context
        final Context mockContext = mockContext();
        // mock original request id
        final RequestId mockOriginalRequestId = mockOriginalRequestId();
        // mock exception
        final Exception mockException = mockException();

        final AuditRequest auditRequest = new RegisterRequestExceptionAuditRequest()
                .userId(mockUserId)
                .resourceId(TEST_RESOURCE_ID)
                .context(mockContext)
                .exception(mockException)
                .service(UserService.class);
        auditRequest.setOriginalRequestId(mockOriginalRequestId);

        // When
        STROOM_AUDIT_SERVICE.audit(auditRequest);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockOriginalRequestId, Mockito.atLeastOnce()).getId();
        verify(mockUserId, Mockito.atLeastOnce()).getId();
        verify(mockContext, Mockito.atLeastOnce()).getPurpose();
        Assert.assertTrue(log.contains(TEST_USER_ID));
        Assert.assertTrue(log.contains(TEST_PURPOSE));
        Assert.assertTrue(log.contains(TEST_ORIGINAL_REQUEST_ID));
        Assert.assertTrue(log.contains(TEST_RESOURCE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_USER_TYPE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_USER_DESCRIPTION));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_USER_OUTCOME_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestResourceException() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        // mock userId
        final UserId mockUserId = mockUserID();
        // mock context
        final Context mockContext = mockContext();
        // mock original request id
        final RequestId mockOriginalRequestId = mockOriginalRequestId();
        // mock exception
        final Exception mockException = mockException();

        final AuditRequest auditRequest = new RegisterRequestExceptionAuditRequest()
                .userId(mockUserId)
                .resourceId(TEST_RESOURCE_ID)
                .context(mockContext)
                .exception(mockException)
                .service(ResourceService.class);
        auditRequest.setOriginalRequestId(mockOriginalRequestId);

        // When
        STROOM_AUDIT_SERVICE.audit(auditRequest);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockOriginalRequestId, Mockito.atLeastOnce()).getId();
        verify(mockUserId, Mockito.atLeastOnce()).getId();
        verify(mockContext, Mockito.atLeastOnce()).getPurpose();
        Assert.assertTrue(log.contains(TEST_USER_ID));
        Assert.assertTrue(log.contains(TEST_PURPOSE));
        Assert.assertTrue(log.contains(TEST_ORIGINAL_REQUEST_ID));
        Assert.assertTrue(log.contains(TEST_RESOURCE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_RESOURCE_TYPE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_RESOURCE_DESCRIPTION));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_RESOURCE_OUTCOME_DESCRIPTION));
    }

    @Test
    public void auditRegisterRequestOtherException() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        // mock userId
        final UserId mockUserId = mockUserID();
        // mock context
        final Context mockContext = mockContext();
        // mock original request id
        final RequestId mockOriginalRequestId = mockOriginalRequestId();
        // mock exception
        final Exception mockException = mockException();

        final AuditRequest auditRequest = new RegisterRequestExceptionAuditRequest()
                .userId(mockUserId)
                .resourceId(TEST_RESOURCE_ID)
                .context(mockContext)
                .exception(mockException)
                .service(PalisadeService.class);
        auditRequest.setOriginalRequestId(mockOriginalRequestId);

        // When
        STROOM_AUDIT_SERVICE.audit(auditRequest);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockOriginalRequestId, Mockito.atLeastOnce()).getId();
        verify(mockUserId, Mockito.atLeastOnce()).getId();
        verify(mockContext, Mockito.atLeastOnce()).getPurpose();
        verify(mockException, Mockito.atLeastOnce()).getMessage();
        Assert.assertTrue(log.contains(TEST_USER_ID));
        Assert.assertTrue(log.contains(TEST_PURPOSE));
        Assert.assertTrue(log.contains(TEST_ORIGINAL_REQUEST_ID));
        Assert.assertTrue(log.contains(TEST_RESOURCE_ID));
        Assert.assertTrue(log.contains(TEST_EXCEPTION_MESSAGE));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_OTHER_TYPE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.REGISTER_REQUEST_EXCEPTION_OTHER_DESCRIPTION));
    }

    @Test
    public void auditReadRequestSuccessful() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        // mock user
        final User mockUser = mockUser();
        // mock context
        final Context mockContext = mockContext();
        // mock original request id
        final RequestId mockOriginalRequestId = mockOriginalRequestId();
        // mock resource
        final LeafResource mockResource = mockResource();
        // mock rules
        final Rules mockRules = mockRules();

        final AuditRequest auditRequest = new ReadRequestCompleteAuditRequest()
                .user(mockUser)
                .resource(mockResource)
                .context(mockContext)
                .rulesApplied(mockRules)
                .numberOfRecordsProcessed(TEST_NUMBER_OF_RECORDS_PROCESSED)
                .numberOfRecordsReturned(TEST_NUMBER_OF_RECORDS_RETURNED);
        auditRequest.setOriginalRequestId(mockOriginalRequestId);

        // When
        STROOM_AUDIT_SERVICE.audit(auditRequest);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockOriginalRequestId, Mockito.atLeastOnce()).getId();
        verify(mockUser, Mockito.atLeastOnce()).getUserId();
        verify(mockResource, Mockito.atLeastOnce()).getId();
        verify(mockResource, Mockito.atLeastOnce()).getType();
        verify(mockContext, Mockito.atLeastOnce()).getPurpose();
        Assert.assertTrue(log.contains(TEST_USER_ID));
        Assert.assertTrue(log.contains(TEST_PURPOSE));
        Assert.assertTrue(log.contains(TEST_ORIGINAL_REQUEST_ID));
        Assert.assertTrue(log.contains(TEST_RESOURCE_ID));
        Assert.assertTrue(log.contains(TEST_DATA_TYPE));
        Assert.assertTrue(log.contains(TEST_RULES_APPLIED));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_COMPLETED_TYPE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_COMPLETED_DESCRIPTION));
    }

    @Test
    public void auditReadRequestTokenException() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        // mock original request id
        final RequestId mockOriginalRequestId = mockOriginalRequestId();
        // mock resource
        final LeafResource mockResource = mockResource();
        // mock exception
        final Exception mockException = Mockito.mock(Exception.class);
        Mockito.doReturn(PalisadeService.TOKEN_NOT_FOUND_MESSAGE).when(mockException).getMessage();

        final AuditRequest auditRequest = new ReadRequestExceptionAuditRequest()
                .resource(mockResource)
                .token(TEST_TOKEN)
                .exception(mockException);
        auditRequest.setOriginalRequestId(mockOriginalRequestId);

        // When
        STROOM_AUDIT_SERVICE.audit(auditRequest);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockOriginalRequestId, Mockito.atLeastOnce()).getId();
        verify(mockResource, Mockito.atLeastOnce()).getId();
        verify(mockResource, Mockito.atLeastOnce()).getType();
        Assert.assertTrue(log.contains(TEST_ORIGINAL_REQUEST_ID));
        Assert.assertTrue(log.contains(TEST_RESOURCE_ID));
        Assert.assertTrue(log.contains(TEST_DATA_TYPE));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_EXCEPTION_TOKEN_TYPE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_EXCEPTION_TOKEN_DESCRIPTION));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_EXCEPTION_TOKEN_OUTCOME_DESCRIPTION));
    }

    @Test
    public void auditReadRequestOtherException() {
        // Given
        Logger.getRootLogger().addAppender(appender);

        // mock original request id
        final RequestId mockOriginalRequestId = mockOriginalRequestId();
        // mock resource
        final LeafResource mockResource = mockResource();
        // mock exception
        final Exception mockException = mockException();

        final AuditRequest auditRequest = new ReadRequestExceptionAuditRequest()
                .resource(mockResource)
                .token(TEST_TOKEN)
                .exception(mockException);
        auditRequest.setOriginalRequestId(mockOriginalRequestId);

        // When
        STROOM_AUDIT_SERVICE.audit(auditRequest);

        //Then
        verify(appender, atLeastOnce()).doAppend(logCaptor.capture());
        final String log = logCaptor.getValue().getRenderedMessage();
        verify(mockOriginalRequestId, Mockito.atLeastOnce()).getId();
        verify(mockResource, Mockito.atLeastOnce()).getId();
        verify(mockResource, Mockito.atLeastOnce()).getType();
        verify(mockException, Mockito.atLeastOnce()).getMessage();
        Assert.assertTrue(log.contains(TEST_ORIGINAL_REQUEST_ID));
        Assert.assertTrue(log.contains(TEST_RESOURCE_ID));
        Assert.assertTrue(log.contains(TEST_DATA_TYPE));
        Assert.assertTrue(log.contains(TEST_EXCEPTION_MESSAGE));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_EXCEPTION_OTHER_TYPE_ID));
        Assert.assertTrue(log.contains(StroomAuditService.READ_REQUEST_EXCEPTION_OTHER_DESCRIPTION));
    }
}