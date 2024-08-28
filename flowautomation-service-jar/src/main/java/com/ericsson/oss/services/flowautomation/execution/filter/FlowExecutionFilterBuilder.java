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

package com.ericsson.oss.services.flowautomation.execution.filter;

import com.ericsson.oss.services.flowautomation.model.FlowExecutionFilter;

/**
 * The type Flow execution filter builder.
 */
public class FlowExecutionFilterBuilder {

    private String flowId;
    private String flowExecutionName;
    private String flowVersion;
    private String username;
    private boolean filterByUser;

    /**
     * Flow id flow execution filter builder.
     *
     * @param flowId the flow id
     * @return the flow execution filter builder
     */
    public FlowExecutionFilterBuilder flowId(final String flowId) {
        this.flowId = flowId;
        return this;
    }

    /**
     * Flow execution name flow execution filter builder.
     *
     * @param flowExecutionName the flow execution name
     * @return the flow execution filter builder
     */
    public FlowExecutionFilterBuilder flowExecutionName(final String flowExecutionName) {
        this.flowExecutionName = flowExecutionName;
        return this;
    }

    /**
     * Flow version flow execution filter builder.
     *
     * @param flowVersion the flow version
     * @return the flow execution filter builder
     */
    public FlowExecutionFilterBuilder flowVersion(final String flowVersion) {
        this.flowVersion = flowVersion;
        return this;
    }

    /**
     * Username flow execution filter builder.
     *
     * @param username the username
     * @return the flow execution filter builder
     */
    public FlowExecutionFilterBuilder user(final String username) {
        this.username = username;
        return this;
    }

    public FlowExecutionFilterBuilder filterByUser(final boolean filterByUser) {
        this.filterByUser = filterByUser;
        return this;
    }

    /**
     * Build flow execution filter.
     *
     * @return the flow execution filter
     */
    public FlowExecutionFilter build() {
        return new FlowExecutionFilter(this.flowId, this.flowExecutionName, this.flowVersion, this.username, filterByUser);
    }
}

