/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.services.flowautomation.rest.providers;

import com.ericsson.oss.services.flowautomation.error.ErrorReason;
import com.ericsson.oss.services.flowautomation.rest.exception.FlowAutomationRestException;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;

import static com.ericsson.oss.services.flowautomation.rest.resource.response.ErrorResponse.ofErrors;

/**
 * The Class FlowAutomationRestExceptionMapper.
 */
@Provider
public class FlowAutomationRestExceptionMapper implements ExceptionMapper<FlowAutomationRestException> {

    /** The logger. */
    @Inject
    private Logger logger;

    @Override
    public Response toResponse(final FlowAutomationRestException exception) {
        final Status httpStatus = exception.getStatus();
        final List<ErrorReason> errorReasons = exception.getErrorReasons();
        logger.error("Returning Error response, status: {}, reasons: {}", httpStatus, errorReasons, exception);
        final String cause = exception.getCause() != null ? exception.getCause().getMessage() : "";
        return Response.status(httpStatus).entity(ofErrors(httpStatus, exception.getMessage(), cause, errorReasons)).build();
    }
}
