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

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.flowautomation.error.ErrorCode;
import com.ericsson.oss.services.flowautomation.error.ErrorCode.UnknownErrorCode;
import com.ericsson.oss.services.flowautomation.error.ErrorReason;

/**
 * The Class FlowAutomationRestException.
 */
public class FlowAutomationRestException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The status. */
    private final Status status;

    /** The error reasons. */
    private final List<ErrorReason> errorReasons;

    /**
     * Instantiates a new flow automation rest exception.
     */
    public FlowAutomationRestException() {
        this(UnknownErrorCode.INSTANCE);
    }

    /**
     * Instantiates a new flow automation rest exception.
     *
     * @param errorCode
     *            the error code
     */
    public FlowAutomationRestException(final ErrorCode errorCode) {
        this(INTERNAL_SERVER_ERROR, errorCode);
    }

    /**
     * Instantiates a new flow automation rest exception.
     *
     * @param status
     *            the status
     * @param errorCode
     *            the error code
     */
    public FlowAutomationRestException(final Status status, final ErrorCode errorCode) {
        this(status, errorCode, errorCode.reason(), (Throwable) null);
    }

    /**
     * Instantiates a new flow automation rest exception.
     *
     * @param status
     *            the status
     * @param errorCode
     *            the error code
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public FlowAutomationRestException(final Status status, final ErrorCode errorCode, final String message, final Throwable cause) {
        this(status, message, cause, Collections.singletonList(ErrorReason.from(errorCode)));
    }

    /**
     * Instantiates a new flow automation rest exception.
     *
     * @param status
     *            the status
     * @param errorReasons
     *            the error reasons
     */
    public FlowAutomationRestException(final Status status, final List<ErrorReason> errorReasons) {
        this(status, status.getReasonPhrase(), (Throwable) null, errorReasons);

    }

    /**
     * Instantiates a new flow automation rest exception.
     *
     * @param status
     *            the status
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public FlowAutomationRestException(final Status status, final String message, final Throwable cause) {
        super(message, cause);
        this.status = status;
        final ErrorReason errorReason = new ErrorReason(message);
        this.errorReasons = singletonList(errorReason);
    }

    /**
     * Instantiates a new flow automation rest exception.
     *
     * @param status
     *            the status
     * @param message
     *            the message
     * @param cause
     *            the cause
     * @param errorReasons
     *            the error reasons
     */
    public FlowAutomationRestException(final Status status, final String message, final Throwable cause, final List<ErrorReason> errorReasons) {
        super(message, cause);
        this.status = status;
        this.errorReasons = unmodifiableList(errorReasons);
    }

    /**
     * Instantiates a new flow automation rest exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public FlowAutomationRestException(final String message, final Throwable cause) {
        this(INTERNAL_SERVER_ERROR, message, cause);
    }

    /**
     * Gets the error reasons.
     *
     * @return the error reasons
     */
    public List<ErrorReason> getErrorReasons() {
        return unmodifiableList(errorReasons);
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
}
