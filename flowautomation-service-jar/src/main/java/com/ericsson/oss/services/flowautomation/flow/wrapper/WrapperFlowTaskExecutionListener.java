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

import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.FLOW_AUTOMATION_EXECUTION;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.getEventType;
import static com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil.convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;

import java.time.Duration;
import java.time.LocalDateTime;

import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper;
import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;

/**
 * The listener interface for receiving wrapperFlowTaskExecution events. The class that is interested in processing a wrapperFlowTaskExecution event
 * implements this interface, and the object created with that class is registered with a component using the component's
 * <code>addWrapperFlowTaskExecutionListener<code> method. When the wrapperFlowTaskExecution event occurs, that object's appropriate method is
 * invoked.
 */
public abstract class WrapperFlowTaskExecutionListener implements ExecutionListener {

    /** The Constant FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME. */
    public static final String FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME = "FAInternal_EXECUTION_STATE";

    private static final Logger LOGGER = LoggerFactory.getLogger(WrapperFlowTaskExecutionListener.class);

    /**
     * Gets the execution state.
     *
     * @return the execution state
     */
    public abstract State getExecutionState();

    @Override
    public void notify(final DelegateExecution execution) throws Exception {
        final State executionState = getExecutionState();
        execution.setVariable(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, executionState.name());
        logSystemRecorder(execution, executionState);
    }

    private void logSystemRecorder(final DelegateExecution execution, final State executionState) {

        final ProcessEngine processEngine = ProcessEngines.getProcessEngine("default");

        final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(execution.getBusinessKey());

        final HistoricProcessInstanceQuery historicProcessInstanceQuery =
                processEngine.getHistoryService().createHistoricProcessInstanceQuery();
        final HistoricProcessInstance historicProcessInstance =
                historicProcessInstanceQuery.processInstanceId(execution.getProcessInstanceId()).singleResult();

        final LocalDateTime startTime = convert(historicProcessInstance.getStartTime());
        final long duration = Duration.between(startTime, LocalDateTime.now()).getSeconds();

        LOGGER.info("Recording {}. More info: {}", FLOW_AUTOMATION_EXECUTION,
                LogRecordingHelper.logGenerateToExecution(getEventType(executionState),
                    flowBusinessKey.getFlowId(),
                    flowBusinessKey.getFlowExecutionName(), Long.toString(duration)));
    }

}
