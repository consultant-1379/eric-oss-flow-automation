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

package com.ericsson.oss.services.flowautomation.error;

import java.io.Serializable;

import com.ericsson.oss.services.flowautomation.error.ErrorCode.UnknownErrorCode;

import lombok.Getter;
import lombok.ToString;

/**
 * The Class ErrorReason used for building error response from exceptions.
 *
 * @see ErrorCode
 */
@Getter
@ToString
public class ErrorReason implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String code;

    private final String errorMessage;

    /**
     * Instantiates a new error reason.
     *
     * @param errorMessage
     *            the error message
     */
    public ErrorReason(final String errorMessage) {
        this.code = UnknownErrorCode.INSTANCE.code();
        this.errorMessage = errorMessage;
    }

    /**
     * Instantiates a new error reason.
     *
     * @param code
     *            the code
     * @param errorMessage
     *            the error message
     */
    public ErrorReason(final String code, final String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates {@code ErrorReason} instance from {@code ErrorCode}.
     *
     * @param errorCode
     *            the error code
     * @return the error reason
     */
    public static ErrorReason from(final ErrorCode errorCode) {
        return new ErrorReason(errorCode.code(), errorCode.reason());
    }
}
