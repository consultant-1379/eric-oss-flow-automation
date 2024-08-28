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

package com.ericsson.oss.services.flowautomation.flow;

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.CANCELLED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.COMPLETED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.CONFIRM_EXECUTE;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.EXECUTING;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.FAILED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.FAILED_EXECUTE;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.FAILED_SETUP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.SETTING_UP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STARTED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOPPED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOPPING;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.SUSPENDED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.slf4j.Logger;

import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * The type Flow execution phase.
 */
public class FlowExecutionPhase {

    @Inject
    @DefaultProcessEngine
    private ProcessEngine processEngine;

    @Inject
    private Logger logger;

    /**
     * An execution in considered in a setup phase if its either in STARTED, SETTING_UP, CONFIRM_EXECUTE.
     *
     * @param executionId the execution id
     * @return the boolean
     */
    public boolean isExecutionInSetupPhase(final String executionId) {
        return isSetupPhase(getExecutionPhase(executionId));
    }

    /**
     * Is execution in review confirm phase boolean.
     *
     * @param executionId the execution id
     * @return the boolean
     */
    public boolean isExecutionInReviewConfirmPhase(final String executionId) {
        final String executionPhase = getExecutionPhase(executionId);
        return CONFIRM_EXECUTE.name().equals(executionPhase);
    }

    /**
     * Is execution suspendable boolean.
     *
     * @param executionId the execution id
     * @return the boolean
     */
    public boolean isExecutionSuspendable(final String executionId) {
        return Stream.of(SUSPENDED, COMPLETED, CANCELLED, STOP, STOPPING, STOPPED, FAILED, FAILED_EXECUTE, FAILED_SETUP)
                .noneMatch(entry -> entry.name().equals(getExecutionPhase(executionId)));
    }

    /**
     * Is execution stoppable boolean.
     *
     * @param executionId the execution id
     * @return the boolean
     */
    public boolean isExecutionStoppable(final String executionId) {
        return isStoppable(getExecutionPhase(executionId));
    }

    /**
     * Gets execution phase.
     *
     * @param executionId the execution id
     * @return the execution phase
     */
    public String getExecutionPhase(final String executionId) {
        final HistoricVariableInstanceQuery historicVariableInstanceQuery = processEngine.getHistoryService().createHistoricVariableInstanceQuery();
        final HistoricVariableInstance historicVariableInstance = historicVariableInstanceQuery.processInstanceId(executionId)
                .variableName(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME).singleResult();
        String executionState = null;
        if (historicVariableInstance != null) {
            executionState = (String) historicVariableInstance.getValue();
        }
        logger.debug("The execution state for executionId: {} is: [{}]", executionId, executionState);
        return executionState;
    }

    /**
     * Checks execution phase is in setup.
     *
     * @param executionPhase the execution phase
     * @return execution phase in setup
     */
    public boolean isSetupPhase(final String executionPhase) {
        return Stream.of(STARTED, SETTING_UP, CONFIRM_EXECUTE)
                .anyMatch(state -> state.name().equals(executionPhase));
    }

    /**
     * Checks execution phase is stoppable.
     *
     * @param executionPhase the execution phase
     * @return execution state is stoppable
     */
    public boolean isStoppable(final String executionPhase) {
        return EXECUTING.name().equals(executionPhase);
    }

    /**
     * Checks if execution phase is final.
     *
     * @param executionPhase the execution phase
     * @return execution state in final state
     */
    public boolean isExecutionInFinalState(final String executionPhase) {
        return Stream.of(COMPLETED, STOPPED, SUSPENDED, CANCELLED, FAILED_SETUP, FAILED_EXECUTE, FAILED)
                .anyMatch(state -> state.name().equals(executionPhase));
    }

    /**
     * Checks if execution phase is deletable.
     *
     * @param executionId the execution id
     * @return execution id deletable
     */
    public boolean isExecutionDeletable(final String executionId) {
        final String executionPhase = getExecutionPhase(executionId);
        return isSetupPhase(executionPhase) || isExecutionInFinalState(executionPhase);
    }
}
