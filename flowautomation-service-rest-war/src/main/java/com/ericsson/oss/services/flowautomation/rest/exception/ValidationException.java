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

package com.ericsson.oss.services.flowautomation.rest.exception;

import com.ericsson.oss.services.flowautomation.error.ErrorReason;

import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * The Class ValidationException.
 */
public class ValidationException extends FlowAutomationRestException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new validation exception.
     *
     * @param errorReasons
     *            the error reasons
     */
    public ValidationException(final List<ErrorReason> errorReasons) {
        super(BAD_REQUEST, errorReasons);
    }
}
