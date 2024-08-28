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

import com.ericsson.oss.services.flowautomation.exception.EntityExistsException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.CONFLICT;

/**
 * The {@code Provider} class for mapping {@link EntityExistsException} to the {@link Response} object.
 */

@Provider
public class EntityExistsExceptionMapper extends AbstractExceptionMapper<EntityExistsException> implements ExceptionMapper<EntityExistsException> {

    @Override
    public Response toResponse(final EntityExistsException exception) {
        return buildResponse(exception);
    }

    @Override
    public Response.Status faErrorCode() {
        return CONFLICT;
    }
}