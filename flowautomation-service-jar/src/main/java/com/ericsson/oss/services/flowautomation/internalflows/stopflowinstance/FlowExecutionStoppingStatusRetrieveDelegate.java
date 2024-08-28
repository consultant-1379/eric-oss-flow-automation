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

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOPPING;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Flow execution status retrieve delegate.
 */
@SuppressWarnings("squid:S2696")
public class FlowExecutionStoppingStatusRetrieveDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionStoppingStatusRetrieveDelegate.class);
    public static final String FLOW_EXECUTION_IDS_MARKED_AS_STOPPING = "flowExecutionIdsMarkedAsStopping";

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.debug("Retrieving flow executions which are marked as STOPPING...");
        String parentProcessInstanceId = getParentProcessInstanceId(execution, execution.getProcessInstanceId());
        final HistoryService historyService = execution.getProcessEngineServices().getHistoryService();
        String executionState = null;
        HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().variableName(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME).processInstanceId(parentProcessInstanceId).singleResult();
        if (historicVariableInstance != null) {
            executionState = (String) historicVariableInstance.getValue();
        }

        if (STOP.name().equals(executionState)) {
            execution.setVariableLocal("stopFlowInstance", true);
            execution.setVariableLocal(FLOW_EXECUTION_IDS_MARKED_AS_STOPPING, Collections.emptyList());
        } else {
            execution.setVariableLocal("stopFlowInstance", false);
            final List<HistoricVariableInstance> processInstancesMarkedAsStopping = historyService.createHistoricVariableInstanceQuery().variableValueEquals(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, STOPPING.name()).list();
            final List<String> flowExecutionIdsMarkedAsStopping = processInstancesMarkedAsStopping.stream()
                    .map(HistoricVariableInstance::getProcessInstanceId).collect(Collectors.toList());
            LOGGER.debug("Number of flow executions marked as STOPPING: [{}]", flowExecutionIdsMarkedAsStopping.size());
            execution.setVariableLocal(FLOW_EXECUTION_IDS_MARKED_AS_STOPPING, flowExecutionIdsMarkedAsStopping);
        }
    }


    /**
     * Gets parent process instance id.
     *
     * @param execution         the execution
     * @param processInstanceId the process instance id
     * @return the parent process instance id
     */
    public String getParentProcessInstanceId(DelegateExecution execution, String processInstanceId) {
        final List<ProcessInstance> processInstances = execution.getProcessEngineServices().getRuntimeService().createProcessInstanceQuery()
                                                               .subProcessInstanceId(processInstanceId).list();
        if (processInstances.isEmpty()) {
            return processInstanceId;
        } else {
            processInstanceId = processInstances.get(0).getId();
            return getParentProcessInstanceId(execution, processInstanceId);
        }
    }
}
