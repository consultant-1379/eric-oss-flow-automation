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

import javax.ejb.ApplicationException;

import com.ericsson.oss.services.flowautomation.error.ErrorCode;

/**
 * The type Flow resource empty exception.
 */
@ApplicationException(rollback = false)
@SuppressWarnings("squid:S110")
public class FlowResourceEmptyException extends FlowResourceNotFoundException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Flow resource empty exception.
     *
     * @param errorCode
     *         the error code
     * @param message
     *         the message
     */
    public FlowResourceEmptyException(final ErrorCode errorCode, final String message) {
        super(errorCode, message);
    }
}
