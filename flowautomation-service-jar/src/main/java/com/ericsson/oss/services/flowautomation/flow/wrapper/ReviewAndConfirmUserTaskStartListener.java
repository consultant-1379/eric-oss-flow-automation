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

package com.ericsson.oss.services.flowautomation.flow.wrapper;

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.CONFIRM_EXECUTE;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * The review and confirm start listener to update the state of the execution to review and confirm.
 */
public class ReviewAndConfirmUserTaskStartListener implements ExecutionListener {

    final ProcessEngine processEngine = ProcessEngines.getProcessEngine("default");

    @Override
    public void notify(final DelegateExecution execution) throws Exception {
        processEngine.getRuntimeService()
                .setVariable(getParentProcessInstanceId(execution.getProcessInstanceId()), FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, CONFIRM_EXECUTE.name());
    }

    private String getParentProcessInstanceId(final String processInstanceId) {
        final List<ProcessInstance> processInstances = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .subProcessInstanceId(processInstanceId)
                .list();
        return processInstances.isEmpty() ? processInstanceId : getParentProcessInstanceId(processInstances.get(0).getId());
    }
}
