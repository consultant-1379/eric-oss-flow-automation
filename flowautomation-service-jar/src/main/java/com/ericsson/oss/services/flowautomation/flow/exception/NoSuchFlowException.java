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
 * The Class NoSuchFlowException.
 */
public class NoSuchFlowException extends FlowAutomationException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new no such flow exception.
     *
     * @param errorCode
     *            the error code
     */
    public NoSuchFlowException(final ErrorCode errorCode) {
        super(errorCode);
    }

}
