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

package com.ericsson.oss.services.flowautomation.entities;

/**
 * The Enum FlowEntityStatus.
 */
public enum FlowEntityStatus {

    /** The flow enabled. */
    ENABLED("enabled"),

    /** The flow disabled. */
    DISABLED("disabled");

    /** The status. */
    private final String status;

    /**
     * Instantiates a new flow entity status.
     *
     * @param status
     *            the status
     */
    FlowEntityStatus(final String status) {
        this.status = status;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

}
