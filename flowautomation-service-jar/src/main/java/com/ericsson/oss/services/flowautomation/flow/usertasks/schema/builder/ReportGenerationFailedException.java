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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;

/**
 * The Class ReportGenerationFailedException.
 */
public class ReportGenerationFailedException extends FlowAutomationException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new report generation failed exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public ReportGenerationFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
