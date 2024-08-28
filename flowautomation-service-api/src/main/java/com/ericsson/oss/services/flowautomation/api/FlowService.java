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

package com.ericsson.oss.services.flowautomation.api;

import java.util.List;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionInput;
import com.ericsson.oss.services.flowautomation.model.FlowSource;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;

/**
 * The Interface FlowService.
 */
public interface FlowService {

    /**
     * Returns a list of imported flows.
     *
     * @return a list of flow description
     */
    List<FlowDefinition> getFlows();

    /**
     * Import a flow package.
     *
     * @param flowPackage
     *         flow package.
     * @return FlowDefinition instance {@link FlowDefinition}.
     */
    FlowDefinition importFlow(byte[] flowPackage);

    /**
     * Import flow flow definition.
     *
     * @param flowPackage
     *         the flow package
     * @param flowSource
     *         the flow source
     * @return the flow definition
     */
    FlowDefinition importFlow(final byte[] flowPackage, FlowSource flowSource);

    /**
     * Execute flow.
     *
     * @param flowId
     *         the flow id
     * @param flowExecutionName
     *         the flow execution name
     */
    void executeFlow(final String flowId, final String flowExecutionName);

    /**
     * Execute flow.
     *
     * @param flowId
     *         the flow id
     * @param flowExecutionInput
     *         the flow execution input
     */
    void executeFlow(final String flowId, final FlowExecutionInput flowExecutionInput);

    /**
     * Activate or Deactivate a specific flow version.
     *
     * @param flowId
     *         flow id.
     * @param flowVersion
     *         flow version.
     * @param activate
     *         true - activate flow with specific version, false - deactivate flow with specific version.
     */
    void activateFlowVersion(final String flowId, final String flowVersion, final boolean activate);

    /**
     * Enable/Disable a flow.
     *
     * @param flowId
     *         flow id.
     * @param enable
     *         true - enable the flow, false - disable the flow.
     */
    void enableFlow(final String flowId, final boolean enable);

    /**
     * Get Flow Summary.
     *
     * @param flowId
     *         flow id
     * @return the flow summary
     */
    FlowDefinition getFlowDefinition(final String flowId);

    /**
     * Gets Flow Input Schema.
     *
     * @param flowId the flow id
     * @return the flow input schema
     */
    String getFlowInputSchema(final String flowId, final String flowVersion);

    /**
     * Delete flow.
     *
     * @param flowId
     *         the flow id
     * @param force
     *         the force
     */
    void deleteFlow(final String flowId, final boolean force);

    /**
     *  Get flow process details.
     *
     * @param flowId
     *         the flow id
     * @param flowVersion
     *         the flow version
     * @return
     *         flow process details
     */
    FlowVersionProcessDetails getProcessDetailsForFlowVersion(final String flowId, final String flowVersion);

}