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

import static com.ericsson.oss.services.flowautomation.rest.resource.response.ErrorResponse.ofErrors;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.ericsson.oss.services.flowautomation.error.ErrorReason;
import com.ericsson.oss.services.flowautomation.exception.ValidationException;

import java.util.List;

/**
 * The {@code Provider} class for mapping {@link ValidationException} to the {@link Response} object.
 */

@Provider
public class ValidationExceptionMapper extends AbstractExceptionMapper<ValidationException> implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(final ValidationException exception){
        logException(exception, Level.DEBUG);
        return buildResponse(exception);
    }

    @Override
    public Response buildResponse(ValidationException exception) {
        final List<ErrorReason> errorReasons = exception.getErrorReasons();
        String cause = exception.getCause() != null ? exception.getCause().getMessage() : "";
        String message = exception.getMessage();

        if (cause.equals("") && message != null) {
            cause = message;
            message = "";
        }

        Response.Status errorCode = faErrorCode();
        return Response.status(errorCode).entity(ofErrors(errorCode, message, cause, errorReasons)).build();
    }

    @Override
    public Response.Status faErrorCode() {
        return BAD_REQUEST;
    }

}
