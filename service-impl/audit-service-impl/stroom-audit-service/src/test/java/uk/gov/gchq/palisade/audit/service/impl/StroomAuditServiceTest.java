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

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.audit.service.AuditService;
import uk.gov.gchq.palisade.audit.service.request.RegisterRequestExceptionAuditRequest;

import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class StroomAuditServiceTest {

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

        final RegisterRequestExceptionAuditRequest auditRequestWithException = new RegisterRequestExceptionAuditRequest();
        Throwable mockException = Mockito.mock(Throwable.class);
        Mockito.doReturn("exception output").when(mockException).getMessage();
        RequestId mockRequestId = Mockito.mock(RequestId.class);
        Mockito.doReturn("originalRequestId").when(mockRequestId).toString();
        auditRequestWithException
                .exception(mockException)
                .service(AuditService.class)
                .originalRequestId(mockRequestId);

        // When
        StroomAuditService stroomAuditService = new StroomAuditService();
        stroomAuditService.audit(auditRequestWithException);

        //Then
        final List<LoggingEvent> logs = logCaptor.getAllValues();
        for (LoggingEvent logEvent : logs) {
            java.lang.System.out.println(logEvent);
        }
    }

}