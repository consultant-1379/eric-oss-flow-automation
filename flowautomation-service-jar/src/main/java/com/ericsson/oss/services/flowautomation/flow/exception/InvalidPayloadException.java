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
 * The Class InvalidPayloadException.
 */
public class InvalidPayloadException extends FlowAutomationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The error code. */
    private final ErrorCode errorCode;

    /**
     * Instantiates a new invalid payload exception.
     *
     * @param errorCode
     *            the error code
     */
    public InvalidPayloadException(final ErrorCode errorCode) {
        this(errorCode, null);
    }

    public InvalidPayloadException(final ErrorCode errorCode, final String errorDetail) {
        super(errorCode, errorDetail);
        this.errorCode = errorCode;
    }


    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the error detail.
     *
     * @return the error detail
     */
    public String getErrorDetail() {
        return super.getMessage();
    }

}
