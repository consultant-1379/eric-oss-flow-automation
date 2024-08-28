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

package com.ericsson.oss.services.flowautomation.internalflows.stopflowinstance;

import static com.ericsson.oss.services.flowautomation.internalflows.stopflowinstance.FlowExecutionStoppingStatusRetrieveDelegate.FLOW_EXECUTION_IDS_MARKED_AS_STOPPING;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Flow execution suspend check delegate.
 */
public class FlowExecutionSuspendCheckDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionSuspendCheckDelegate.class);
    public static final String SUSPENDED_FLOW_EXECUTION_IDS = "suspendedFlowExecutionIds";

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        final List<String> flowExecutionIdsMarkedAsStopping = (List<String>) execution.getVariableLocal(FLOW_EXECUTION_IDS_MARKED_AS_STOPPING);
        LOGGER.debug("Number of Flow executions marked as STOPPING: [{}]", flowExecutionIdsMarkedAsStopping.size());
        final HistoryService historyService = execution.getProcessEngineServices().getHistoryService();
        final List<String> suspendedFlowExecutionIds = flowExecutionIdsMarkedAsStopping.stream()
                //No active children processes for the flow execution
                .filter(id -> historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(id).active().count() == 0)
                //The status of flow execution itself is suspended
                .filter(id -> historyService.createHistoricProcessInstanceQuery().processInstanceId(id).suspended().count() > 0)
                .collect(Collectors.toList());

        execution.setVariableLocal(SUSPENDED_FLOW_EXECUTION_IDS, suspendedFlowExecutionIds);
    }
}
