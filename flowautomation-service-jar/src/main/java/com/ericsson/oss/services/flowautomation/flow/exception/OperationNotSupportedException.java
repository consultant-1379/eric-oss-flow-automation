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
 * The Class OperationNotSupportedException.
 */
public class OperationNotSupportedException extends FlowAutomationException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code OperationNotSupportedException} with error code and message.
     *
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     */
    public OperationNotSupportedException(final ErrorCode errorCode, final String message) {
        super(errorCode, message, null);
    }
}
