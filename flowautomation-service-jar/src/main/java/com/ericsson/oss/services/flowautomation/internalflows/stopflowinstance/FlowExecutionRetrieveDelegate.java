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

import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;

/**
 * The type Flow execution retrieve delegate.
 */
public class FlowExecutionRetrieveDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionRetrieveDelegate.class);
    public static final String PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOP = "parentProcessInstanceIdsMarkedAsStop";
    public static final String PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOPPING = "parentProcessInstanceIdsMarkedAsStopping";

    @Override
    @SuppressWarnings("squid:S2696")
    public void execute(final DelegateExecution execution) throws Exception {

        final ProcessInstance parentProcessInstance = execution.getProcessEngineServices().getRuntimeService().createProcessInstanceQuery().subProcessInstanceId(execution.getProcessInstanceId()).singleResult();
        String parentProcessInstanceId = parentProcessInstance.getId();

        LOGGER.debug("Retrieving flow executions which are marked as STOP, STOPPING...");
        final List<String> parentProcessInstanceIdsRequestedToStop = getProcessInstancesInState(execution, STOP);
        if (parentProcessInstanceIdsRequestedToStop.contains(parentProcessInstanceId)) {
            execution.setVariableLocal("stopFlowInstance", true);
            execution.setVariableLocal(PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOP, Collections.emptyList());
            execution.setVariableLocal(PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOPPING, Collections.emptyList());
        } else {
            execution.setVariableLocal("stopFlowInstance", false);
            execution.setVariableLocal(PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOP, parentProcessInstanceIdsRequestedToStop);
            execution.setVariableLocal(PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOPPING, getProcessInstancesInState(execution, STOPPING));
        }
    }

    private List<String> getProcessInstancesInState(final DelegateExecution execution, final State state) {
        final HistoryService historyService = execution.getProcessEngineServices().getHistoryService();
        final List<HistoricVariableInstance> processInstancesInRequestedState = historyService.createHistoricVariableInstanceQuery().variableValueEquals(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, state.name()).list();
        final List<String> parentProcessInstanceIdsInRequestedState = processInstancesInRequestedState.stream()
                .map(HistoricVariableInstance::getProcessInstanceId)
                .collect(Collectors.toList());
        LOGGER.debug("Number of flow executions marked as [{}] : [{}]", state, parentProcessInstanceIdsInRequestedState.size());
        return parentProcessInstanceIdsInRequestedState;
    }
}
