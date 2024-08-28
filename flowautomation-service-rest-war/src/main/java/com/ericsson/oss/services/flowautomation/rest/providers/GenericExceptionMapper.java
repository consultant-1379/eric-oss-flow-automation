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

import com.ericsson.oss.services.flowautomation.rest.resource.response.ErrorResponse;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.util.stream.Stream;

import static javax.ws.rs.core.Response.serverError;


/**
 * The type Generic exception mapper class to map any {@link Exception} to HTTP {@link Response}.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    private Logger logger;

    @Context
    private Providers providers;

    /**
     * This method maps any exceptions thrown out from flowautomation services to HTTP {@code Response}.
     * <p>
     * It first gets the actual root cause of the exception and then tries to find the exception mapper for it. throws serverError if no mapper is found.
     *
     * @param throwable
     * @return
     */
    @Override
    public Response toResponse(final Throwable throwable) {
        logger.debug("Received exception, details:", throwable);
        final Throwable rootCause = Stream.iterate(throwable, Throwable::getCause)
                .filter(element -> element.getCause() == null).findFirst().orElse(throwable);

        final Class<? extends Throwable> rootCauseType = rootCause.getClass();
        final ExceptionMapper mapper = providers.getExceptionMapper(rootCauseType);
        if (mapper == null || GenericExceptionMapper.class.isAssignableFrom(mapper.getClass())) {
            logger.warn("No Response mapper found for the Exception: [{}]", rootCauseType);
            return serverError().entity(ErrorResponse.of(rootCause.getMessage())).build();
        }

        logger.info("Invoking the toResponse for exception: [{}] using mapper class: [{}]", rootCauseType, mapper);
        return mapper.toResponse(rootCause);
    }
}
