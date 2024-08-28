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

package com.ericsson.oss.services.flowautomation.flowapi.usertask;

import static java.util.Collections.singletonList;

import com.ericsson.oss.services.flowautomation.error.ErrorReason;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;

/**
 * The type Usertask input processing error.
 * It can be used by flow designers to validate or process the input from the usertasks.
 * <p>
 * This {@code UsertaskInputProcessingError} should be only thrown from scripts tasks when the user task and subsequent script task are synchronous, and hence they are in the same transaction.
 */
public class UsertaskInputProcessingError extends FlowAutomationException {
    private static final long serialVersionUID = 2071245113412232652L;

    /**
     * Instantiates a new Usertask input processing error.
     *
     * @param code    the code
     * @param message the message
     */
    public UsertaskInputProcessingError(final String code, final String message) {
        super(singletonList(new ErrorReason(code, message)), message, null);
    }
}
