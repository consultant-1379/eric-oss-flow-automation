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

import com.ericsson.oss.services.flowautomation.error.ErrorCode;

/**
 * The exception that indicates a failure while going back to a previous task..
 */
public class UserTaskBackException extends FlowAutomationException {

    private static final long serialVersionUID = 1L;


    public UserTaskBackException(final ErrorCode errorCode) {
        super(errorCode);
    }

    public UserTaskBackException(final ErrorCode errorCode, final Throwable cause) {
        super(errorCode, cause);
    }
}
