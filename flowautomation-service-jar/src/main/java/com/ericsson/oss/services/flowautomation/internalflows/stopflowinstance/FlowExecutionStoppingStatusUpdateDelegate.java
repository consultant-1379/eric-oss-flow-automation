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

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOPPING;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;
import static com.ericsson.oss.services.flowautomation.internalflows.stopflowinstance.FlowExecutionRetrieveDelegate.PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOP;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Flow execution status update delegate.
 */
public class FlowExecutionStoppingStatusUpdateDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionStoppingStatusUpdateDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        final List<String> parentProcessInstanceIdsMarkedAsStop = (List<String>) execution.getVariableLocal(PARENT_PROCESS_INSTANCE_IDS_MARKED_AS_STOP);
        if (!parentProcessInstanceIdsMarkedAsStop.isEmpty()) {
            LOGGER.debug("Number of flow executions whose state will be updated from STOP to STOPPING: [{}]", parentProcessInstanceIdsMarkedAsStop.size());
            parentProcessInstanceIdsMarkedAsStop.forEach(id -> execution.getProcessEngineServices().getRuntimeService().setVariable(id, FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, STOPPING.name()));
        }
    }
}
