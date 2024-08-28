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

package com.ericsson.oss.services.flowautomation.flow.exception;

import com.ericsson.oss.services.flowautomation.error.ErrorCode;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;

/**
 * Thrown when an error occurs using FlowService.
 */
public class FlowServiceException extends FlowAutomationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new flow service exception.
     *
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     */
    public FlowServiceException(final ErrorCode errorCode, final String message) {
        super(errorCode, message);
    }

    /**
     * Instantiates a new flow service exception.
     *
     * @param errorCode
     *            the errorCode
     * @param cause
     *            the cause
     */
    public FlowServiceException(final ErrorCode errorCode, final Throwable cause) {
        super(errorCode, errorCode.reason(), cause);
    }

    /**
     * Instantiates a new flow service exception..
     *
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public FlowServiceException(final ErrorCode errorCode, final String message, final Throwable cause) {
        super(errorCode, message, cause);
    }

}
