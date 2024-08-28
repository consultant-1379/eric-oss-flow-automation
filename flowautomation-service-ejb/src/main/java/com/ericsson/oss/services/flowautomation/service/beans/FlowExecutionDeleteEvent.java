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

package com.ericsson.oss.services.flowautomation.service.beans;

import lombok.Getter;

/**
 * The type Flow execution delete event.
 */
@Getter
public class FlowExecutionDeleteEvent {

    private final String flowId;
    private final String flowExecutionName;

    /**
     * Instantiates a new Flow execution delete event.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     */
    public FlowExecutionDeleteEvent(final String flowId, final String flowExecutionName) {
        this.flowId = flowId;
        this.flowExecutionName = flowExecutionName;
    }

}
