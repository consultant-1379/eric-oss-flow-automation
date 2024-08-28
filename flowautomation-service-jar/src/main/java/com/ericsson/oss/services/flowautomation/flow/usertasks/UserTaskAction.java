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

package com.ericsson.oss.services.flowautomation.flow.usertasks;

/**
 * The Enum UserTaskAction.
 */
public enum UserTaskAction {

    EXECUTE("Execute"),

    BACK("Back"),

    PREVIEW("Preview"),

    CONTINUE("Continue"),

    SUBMIT("Submit");

    private final String value;

    /**
     * Instantiates a new user task action.
     *
     * @param value
     *            the value
     */
    UserTaskAction(final String value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
