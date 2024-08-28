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

import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.ReportGenerationFailedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * The {@code Provider} class for mapping {@link ReportGenerationFailedException} to the {@link Response} object.
 */

@Provider
public class ReportGenerationFailedExceptionMapper extends AbstractExceptionMapper<ReportGenerationFailedException> implements ExceptionMapper<ReportGenerationFailedException> {

    @Override
    public Response toResponse(final ReportGenerationFailedException exception){
        logException(exception, Level.DEBUG);
        return buildResponse(exception);
    }

    @Override
    public Response.Status faErrorCode() {
        return INTERNAL_SERVER_ERROR;
    }

}