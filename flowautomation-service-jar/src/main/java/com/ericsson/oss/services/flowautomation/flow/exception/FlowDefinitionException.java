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
 * Thrown when a Flow Package (zip) has invalid content.
 */
public class FlowDefinitionException extends FlowAutomationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new invalid flow package exception.
     */
    public FlowDefinitionException() {
        super();
    }

    /**
     * Instantiates a new invalid flow package exception.
     *
     * @param errorCode
     *            the error code
     */
    public FlowDefinitionException(final ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Instantiates a new invalid flow package exception.
     *
     * @param errorCode
     *            the error code
     * @param cause
     *            the cause
     */
    public FlowDefinitionException(final ErrorCode errorCode, final Throwable cause) {
        super(errorCode, cause);
    }
}
