package uk.gov.gchq.palisade.audit.service.impl;


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.audit.service.request.ExceptionAuditRequest;

import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class StroomSimpleAuditServiceTest {

    @Mock
    AppenderSkeleton appender;

    @Captor
    ArgumentCaptor<LoggingEvent> logCaptor;

    @Test
    public void audit() {
        // Given
        //this test needs to be rewritten to use slf4j - and interept the loggerFactory for event-logger
        //top level pom.xml includes log4j - log4j is deprecated by slf4j.
        //The spring boot changes will convert all calls to log4j into slf4j


        Logger.getRootLogger().addAppender(appender);

        final ExceptionAuditRequest auditRequestWithException = new ExceptionAuditRequest();
        Throwable mockException = Mockito.mock(Throwable.class);
        Mockito.doReturn("exception output").when(mockException).getMessage();
        Context mockContext = Mockito.mock(Context.class);
        Mockito.doReturn("purpose").when(mockContext).getPurpose();
        UserId mockUserId = Mockito.mock(UserId.class);
        Mockito.doReturn("UserId string").when(mockUserId).getId();
        String resourceId = "ResourceId";
        String id = "Id";
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn("origRequestId").when(mockRequestId).toString();
        auditRequestWithException
                .exception(mockException)
                .context(mockContext, ExceptionAuditRequest.class)
                .userId(mockUserId, ExceptionAuditRequest.class)
                .resourceId(resourceId, ExceptionAuditRequest.class)
                .id(id)
                .originalRequestId(mockRequestId);

        // When
        StroomSimpleAuditService stroomSimpleAuditService = new StroomSimpleAuditService();
        stroomSimpleAuditService.audit(auditRequestWithException);

        //Then
        final List<LoggingEvent> logs = logCaptor.getAllValues();
        for (LoggingEvent logEvent : logs) {
            java.lang.System.out.println(logEvent);
        }
    }
}