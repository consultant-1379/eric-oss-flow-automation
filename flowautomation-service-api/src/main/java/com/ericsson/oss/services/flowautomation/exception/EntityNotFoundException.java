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

package com.ericsson.oss.services.flowautomation.exception;

import com.ericsson.oss.services.flowautomation.error.ErrorCode;

/**
 * Thrown to indicate an entity does not exist in Database. For example, if a flow does not exist, an instance of this class
 * {@code EntityNotFoundException} can be thrown.
 */
public class EntityNotFoundException extends FlowAutomationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new entity not found exception.
     *
     * @param message
     *            the message
     */
    public EntityNotFoundException(final String message) {
        super(message, null);
    }

    /**
     * Constructs an {@code EntityNotFoundException} with error code and message.
     *
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     */
    public EntityNotFoundException(final ErrorCode errorCode, final String message) {
        super(errorCode, message, null);
    }

    /**
     * Constructs an {@code EntityNotFoundException} with error code, message and cause.
     *
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public EntityNotFoundException(final ErrorCode errorCode, final String message, final Throwable cause) {
        super(errorCode, message, cause);
    }
}
