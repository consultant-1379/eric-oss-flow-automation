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

package com.ericsson.oss.services.flowautomation.rest.resource.response;

import com.ericsson.oss.services.flowautomation.error.ErrorReason;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * The Class ErrorResponse.
 */
public class ErrorResponse {

    /** The status. */
    private final int status;

    /** The reason phrase. */
    private final String reasonPhrase;

    /** The error detail. */
    private final String errorDetail;

    /** The errors. */
    private final List<ErrorReason> errors;

    /**
     * Instantiates a new error response.
     *
     * @param status
     *            the status
     * @param reasonPhrase
     *            the reason phrase
     * @param errorDetail
     *            the error detail
     * @param errors
     *            the errors
     */
    private ErrorResponse(final int status, final String reasonPhrase, final String errorDetail, final List<ErrorReason> errors) {
        this.status = status;
        this.reasonPhrase = reasonPhrase;
        this.errorDetail = errorDetail;
        this.errors = Collections.unmodifiableList(errors);
    }

    /**
     * Of.
     *
     * @param status
     *            the status
     * @param errorDetail
     *            the error detail
     * @param error
     *            the error
     * @return the error response
     */
    public static ErrorResponse of(final Response.Status status, final String errorDetail, final ErrorReason error) {
        return ofErrors(status, errorDetail, Collections.singletonList(error));
    }


    /**
     * Of error response.
     *
     * @param errorDetail the error detail
     * @return the error response
     */
    public static ErrorResponse of(final String errorDetail) {
        return of(INTERNAL_SERVER_ERROR, errorDetail, new ErrorReason(errorDetail));
    }

    /**
     * Of errors.
     *
     * @param status
     *            the status
     * @param errorDetail
     *            the error detail
     * @param errors
     *            the errors
     * @return the error response
     */
    public static ErrorResponse ofErrors(final Response.Status status, final String errorDetail, final List<ErrorReason> errors) {
        return new ErrorResponse(status.getStatusCode(), status.getReasonPhrase(), errorDetail, errors);
    }

    /**
     * Of errors.
     *
     * @param status
     *            the status
     * @param reasonPhrase
     *            the reason phrase
     * @param errorDetail
     *            the error detail
     * @param errors
     *            the errors
     * @return the error response
     */
    public static ErrorResponse ofErrors(final Response.Status status, final String reasonPhrase, final String errorDetail,
            final List<ErrorReason> errors) {
        return new ErrorResponse(status.getStatusCode(), reasonPhrase, errorDetail, errors);
    }

    /**
     * Gets the errors.
     *
     * @return the errors
     */
    public List<ErrorReason> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Gets the reason phrase.
     *
     * @return the reason phrase
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Gets the error detail.
     *
     * @return the error detail
     */
    @JsonInclude(Include.NON_EMPTY)
    public String getErrorDetail() {
        return errorDetail;
    }

    @Override
    public String toString() {
        return format("ErrorResponse [status=%s, reasonPhrase=%s, errorDetail=%s, errors=%s]", status, reasonPhrase, errorDetail, errors);
    }
}
