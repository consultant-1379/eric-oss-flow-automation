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

import static com.ericsson.oss.services.flowautomation.internalflows.stopflowinstance.FlowExecutionRetrieveDelegate.PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOP;
import static com.ericsson.oss.services.flowautomation.internalflows.stopflowinstance.FlowExecutionRetrieveDelegate.PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOPPING;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Flow execution child process instances retrieve delegate.
 */
public class FlowExecutionSubProcessInstancesRetrieveDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionSubProcessInstancesRetrieveDelegate.class);
    public static final String PROCESS_INSTANCE_IDS_TO_BE_SUSPENDED = "processInstanceIdsToBeSuspended";

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.debug("Retrieving sub process instances ids from parent process instance ids...");
        final RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

        final List<String> processInstanceIdsToBeSuspended = new ArrayList<>();
        final List<String> parentProcessInstanceIdsMarkedAsStop = (List<String>) execution.getVariableLocal(PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOP);
        parentProcessInstanceIdsMarkedAsStop.forEach(id -> collectSubProcessInstancesIds(processInstanceIdsToBeSuspended, runtimeService, id));

        final List<String> parentProcessInstanceIdsMarkedAsStopping = (List<String>) execution.getVariableLocal(PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOPPING);
        parentProcessInstanceIdsMarkedAsStopping.forEach(id -> collectSubProcessInstancesIds(processInstanceIdsToBeSuspended, runtimeService, id));

        LOGGER.debug("Number of process instances to be suspended: [{}]", processInstanceIdsToBeSuspended.size());
        execution.setVariableLocal(PROCESS_INSTANCE_IDS_TO_BE_SUSPENDED, processInstanceIdsToBeSuspended);
    }

    private void collectSubProcessInstancesIds(final List<String> processInstanceIdsToBeSuspended, final RuntimeService runtimeService, final String parentProcessInstanceId) {
        final List<String> subProcessInstanceIds = new ArrayList<>();
        getSubProcessInstancesIds(runtimeService, parentProcessInstanceId, subProcessInstanceIds);
        processInstanceIdsToBeSuspended.add(parentProcessInstanceId);
        processInstanceIdsToBeSuspended.addAll(subProcessInstanceIds);
        LOGGER.debug("Number of sub process instances to be suspended is: [{}] for parent process instance: [{}]", subProcessInstanceIds.size(), parentProcessInstanceId);
    }

    @SuppressWarnings("squid:S1612")
    private List<String> getSubProcessInstancesIds(final RuntimeService runtimeService, final String parentProcessInstanceId, final List<String> subProcessInstanceIds) {
        final List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().superProcessInstanceId(parentProcessInstanceId).active().list();
        subProcessInstanceIds.addAll(processInstances.stream().map(Execution::getId).collect(Collectors.toList()));
        processInstances.forEach(processInstance -> getSubProcessInstancesIds(runtimeService, processInstance.getId(), subProcessInstanceIds));
        return subProcessInstanceIds;
    }
}
