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

import lombok.Getter;

/**
 * Flow version process details
 */
@Getter
public class FlowVersionProcessDetails {

    private final String processId;
    private final String setupProcessId;
    private final String executeProcessId;

    /**
     * @param processId
     * @param setupProcessId
     * @param executeProcessId
     */
    public FlowVersionProcessDetails(String processId, String setupProcessId, String executeProcessId) {
        this.processId = processId;
        this.setupProcessId = setupProcessId;
        this.executeProcessId = executeProcessId;
    }
}
