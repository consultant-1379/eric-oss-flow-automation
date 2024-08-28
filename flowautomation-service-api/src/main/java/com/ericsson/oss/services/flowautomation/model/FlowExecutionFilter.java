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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The type Flow execution filter.
 */
@Getter
@EqualsAndHashCode
@ToString
public class FlowExecutionFilter {

    private final String flowId;
    private final String flowExecutionName;
    private final String flowVersion;
    private final String user;
    private final boolean filterByUser;

    public FlowExecutionFilter(final String flowId, final String flowExecutionName, final String flowVersion, final String user, final boolean filterByUser) {
        this.flowId = flowId;
        this.flowExecutionName = flowExecutionName;
        this.flowVersion = flowVersion;
        this.user = user;
        this.filterByUser = filterByUser;
    }

}
