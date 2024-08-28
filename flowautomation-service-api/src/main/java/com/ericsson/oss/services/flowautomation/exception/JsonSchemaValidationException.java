/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

public class JsonSchemaValidationException extends FlowAutomationException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code JsonSchemaValidationException} with an error message.
     *
     * @param message
     *          the message
     */
    public JsonSchemaValidationException(final String message) {
        super(message, null);
    }

}
