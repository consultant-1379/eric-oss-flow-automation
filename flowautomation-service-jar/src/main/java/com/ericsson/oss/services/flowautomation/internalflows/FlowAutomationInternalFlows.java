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

package com.ericsson.oss.services.flowautomation.internalflows;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class FlowAutomationInternalFlows.
 */
public class FlowAutomationInternalFlows {

    /**
     * The constant ERICSSON.
     */
    public static final String ERICSSON = "#Ericsson";
    /** The Constant flowAutomationInternalFlowsId. */
    private static final Map<String, String> flowAutomationInternalFlowsId = new HashMap<>();

    private static final Map<String, String> flowAutomationInternalFlowsIdVersion = new HashMap<>();

    static {
        flowAutomationInternalFlowsId.put("com.ericsson.oss.fa.internal.flows.houseKeeping", "FA_HOUSE_KEEPING");
        flowAutomationInternalFlowsId.put("com.ericsson.oss.fa.internal.flows.stopflowinstance", "FA_STOP_FLOW_INSTANCE");
        flowAutomationInternalFlowsId.put("com.ericsson.oss.fa.internal.flows.incidentHandling", "FA_INCIDENT_HANDLING");
    }

    /**
     * Instantiates a new flow automation internal flows.
     */
    private FlowAutomationInternalFlows() {
        //
    }

    /**
     * Checks if is internal flow.
     *
     * @param flowId the flow id
     * @return true, if is internal flow
     */
    public static boolean isInternalFlow(final String flowId) {
        return flowAutomationInternalFlowsId.containsKey(flowId);
    }


    /**
     * Gets flow execution name.
     *
     * @param flowId      the flow id
     * @param flowVersion the flow version
     * @return the flow execution name
     */
    public static String getFlowExecutionName(final String flowId, final String flowVersion) {
        return getFlowExecutionName(flowId) + "_"+ flowVersion;
    }

    /**
     * Gets flow execution name without version.
     *
     * @param flowId      the flow id
     * @return the flow execution name
     */
    public static String getFlowExecutionName(final String flowId) {
        return flowAutomationInternalFlowsId.get(flowId);
    }


    /**
     * Cacheflow id and version.
     *
     * @param flowId      the flow id
     * @param flowVersion the flow version
     */
    public static void cacheInternalflowIdAndVersion(final String flowId, final String flowVersion) {
        flowAutomationInternalFlowsIdVersion.put(flowId, flowVersion);
    }

}
