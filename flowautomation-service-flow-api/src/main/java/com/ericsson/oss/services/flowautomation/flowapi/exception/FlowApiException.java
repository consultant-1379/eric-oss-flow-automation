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

package com.ericsson.oss.services.flowautomation.flowapi.exception;

/**
 * The type Flow api exception.
 */
public class FlowApiException extends RuntimeException {

    private static final long serialVersionUID = -8685516649559072098L;

    /**
     * Instantiates a new Flow api exception.
     *
     * @param message
     *         the message
     */
    public FlowApiException(final String message) {
        super(message);
    }
}
