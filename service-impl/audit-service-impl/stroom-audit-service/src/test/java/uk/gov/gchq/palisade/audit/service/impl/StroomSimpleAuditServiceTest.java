package uk.gov.gchq.palisade.audit.service.impl;


import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.audit.service.request.ExceptionAuditRequest;

public class StroomSimpleAuditServiceTest {

    @Test
    public void audit() {
        // Given
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

        // Then
    }
}