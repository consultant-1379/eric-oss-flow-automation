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

import java.util.Collections;
import java.util.List;

import javax.ejb.ApplicationException;

import com.ericsson.oss.services.flowautomation.error.ErrorCode;
import com.ericsson.oss.services.flowautomation.error.ErrorCode.UnknownErrorCode;
import com.ericsson.oss.services.flowautomation.error.ErrorReason;

/**
 * This is the base class for handling all the flow automation exceptions. it stores the {@code ErrorCode} which is then used by Web layer to create
 * the error response.
 */
@ApplicationException(rollback = true)
public class FlowAutomationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The error reasons. */
    private final List<ErrorReason> errorReasons;

    /**
     * Instantiates a new flow automation exception.
     */
    public FlowAutomationException() {
        this(UnknownErrorCode.INSTANCE);
    }

    /**
     * Instantiates a new flow automation exception.
     *
     * @param errorCode
     *         the error code
     */
    public FlowAutomationException(final ErrorCode errorCode) {
        this(errorCode, (Throwable) null);
    }

    /**
     * Instantiates a new flow automation exception.
     *
     * @param message
     *         the message
     */
    public FlowAutomationException(final String message) {
        this(message, (Throwable) null);
    }

    /**
     * Instantiates a new flow automation exception.
     *
     * @param cause
     *         the cause
     */
    public FlowAutomationException(final Throwable cause) {
        this(cause.getMessage(), cause);
    }

    /**
     * Instantiates a new flow automation exception.
     *
     * @param errorCode
     *         the error code
     * @param message
     *         the message
     */
    public FlowAutomationException(final ErrorCode errorCode, final String message) {
        this(errorCode, message, (Throwable) null);
    }

    /**
     * Instantiates a new flow automation exception.
     *
     * @param errorCode
     *         the error code
     * @param cause
     *         the cause
     */
    public FlowAutomationException(final ErrorCode errorCode, final Throwable cause) {
        this(errorCode, errorCode.reason(), cause);
    }

    /**
     * Instantiates a new flow automation exception.
     *
     * @param message
     *         the message
     * @param cause
     *         the cause
     */
    public FlowAutomationException(final String message, final Throwable cause) {
        this(UnknownErrorCode.INSTANCE, message, cause);
    }

    /**
     * Instantiates a new flow automation exception.
     *
     * @param errorCode
     *         the error code
     * @param message
     *         the message
     * @param cause
     *         the cause
     */
    public FlowAutomationException(final ErrorCode errorCode, final String message, final Throwable cause) {
        this(Collections.singletonList(ErrorReason.from(errorCode)), message, cause);
    }

    /**
     * Instantiates a new flow automation exception.
     *
     * @param errorReasons
     *         a list of error reasons.
     * @param message
     *         the message
     * @param cause
     *         the cause
     */
    public FlowAutomationException(final List<ErrorReason> errorReasons, final String message, final Throwable cause) {
        super(message, cause);
        this.errorReasons = Collections.unmodifiableList(errorReasons);
    }

    /**
     * Gets the error reasons.
     *
     * @return the error reasons
     */
    public List<ErrorReason> getErrorReasons() {
        return errorReasons;
    }
}