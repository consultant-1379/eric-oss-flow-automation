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
 * Thrown to indicate an entity exists already in Database. For example, if a flow already exists, an instance of this class
 * {@code EntityExistsException} can be thrown.
 */
public class EntityExistsException extends FlowAutomationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code EntityExistsException} with error code and message.
     *
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     */
    public EntityExistsException(final ErrorCode errorCode, final String message) {
        super(errorCode, message, null);
    }
}
