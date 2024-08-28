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
 * The Class FlowResourceNotFoundException.
 */
public class FlowResourceNotFoundException extends FlowAutomationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new flow resource not found exception.
     *
     * @param cause
     *            the cause
     */
    public FlowResourceNotFoundException(final Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new flow resource not found exception.
     *
     * @param errorCode
     *            the error code
     */
    public FlowResourceNotFoundException(final ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Instantiates a new flow resource not found exception.
     *
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     */
    public FlowResourceNotFoundException(final ErrorCode errorCode, final String message) {
        super(errorCode, message);
    }

    /**
     * Instantiates a new flow resource not found exception.
     *
     * @param errorCode
     *            the error code
     * @param cause
     *            the cause
     */
    public FlowResourceNotFoundException(final ErrorCode errorCode, final Throwable cause) {
        super(errorCode, errorCode.reason(), cause);
    }

    /**
     * Instantiates a new flow resource not found exception.
     *
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public FlowResourceNotFoundException(final ErrorCode errorCode, final String message, final Throwable cause) {
        super(errorCode, message, cause);
    }
}
