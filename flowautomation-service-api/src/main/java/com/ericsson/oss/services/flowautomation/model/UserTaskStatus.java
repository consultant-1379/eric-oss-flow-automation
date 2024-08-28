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

package com.ericsson.oss.services.flowautomation.model;

import java.util.Arrays;

/**
 * The Enum UserTaskStatus.
 */
public enum UserTaskStatus {

    ACTIVE("active"), COMPLETED("completed"), OTHER("other");

    private final String value;

    /**
     * Instantiates a new user task status.
     *
     * @param value the enumeration value
     */
    UserTaskStatus(final String value) {
        this.value = value;
    }

    /**
     * Gets the enumeration value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }


    /**
     * Returns the Enum for a string status.
     *
     * @param status the status
     * @return the user task status
     */
    public static UserTaskStatus valueFrom(final String status) {
        return Arrays.stream(UserTaskStatus.values())
                .filter(entry -> entry.value.equals(status))
                .findAny()
                .orElse(OTHER);
    }
}
