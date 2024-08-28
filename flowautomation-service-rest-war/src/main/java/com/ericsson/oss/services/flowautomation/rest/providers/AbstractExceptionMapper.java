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
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static com.ericsson.oss.services.flowautomation.rest.resource.response.ErrorResponse.ofErrors;

public abstract class AbstractExceptionMapper<E extends FlowAutomationException>{

    /** The logger. */
    @Inject
    protected Logger logger;

    enum Level {
        DEBUG,
        INFO,
        ERROR
    }


    public Response buildResponse(final E exception) {
        final List<ErrorReason> errorReasons = exception.getErrorReasons();
        final String cause = exception.getCause() != null ? exception.getCause().getMessage() : "";

        Status errorCode = faErrorCode();
        return  Response.status(errorCode).entity(ofErrors(errorCode, exception.getMessage(), cause, errorReasons)).build();
    }

    public abstract Status faErrorCode();

    public void logException(E exception, Level level) {
        String exceptionDetails = "Exception details:";
        switch (level){
            case DEBUG :
                logger.debug(exceptionDetails, exception);
                break;
            case INFO :
                logger.info(exceptionDetails, exception);
                break;
            case ERROR :
                logger.error(exceptionDetails, exception);
                break;
        }

    }
}
