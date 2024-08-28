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
package com.ericsson.oss.services.flowautomation.test.fwk.check;

import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.test.fwk.AbstractFlowInputCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.query.FlowAutomationQueries;
import com.ericsson.oss.services.flowautomation.test.fwk.util.SimpleRetryManager;

public class FlowAutomationCheckWithRetries extends FlowAutomationCheckImpl implements FlowAutomationCheck {

    protected SimpleRetryManager retryManager = new SimpleRetryManager();

    public FlowAutomationCheckWithRetries(FlowService flowService, FlowExecutionService flowExecutionService, FlowAutomationQueries flowAutomationQueries) {
        super(flowService, flowExecutionService, flowAutomationQueries);
    }

    @Override
    public void checkNumberOfFlowsImported(int number) {
        retryManager.executeWithRetries(() -> {
            super.checkNumberOfFlowsImported(number);
            return null;
        });
    }

    @Override
    public void checkNumberOfExecutions(int number) {
        retryManager.executeWithRetries(() -> {
            super.checkNumberOfExecutions(number);
            return null;
        });
    }

    @Override
    public void checkNumberOfActiveExecutions(int number) {
        retryManager.executeWithRetries(() -> {
            super.checkNumberOfActiveExecutions(number);
            return null;
        });
    }

    @Override
    public void checkNumberOfExecutionsForFlow(String flowId, int number) {
        retryManager.executeWithRetries(() -> {
            super.checkNumberOfExecutionsForFlow(flowId, number);
            return null;
        });
    }

    @Override
    public void checkNumberOfActiveExecutionsForFlow(String flowId, int number) {
        retryManager.executeWithRetries(() -> {
            super.checkNumberOfActiveExecutionsForFlow(flowId, number);
            return null;
        });
    }

    @Override
    public void checkExecutionIsDeleted(String flowId, String executionName) {
        retryManager.executeWithRetries(() -> {
            super.checkExecutionIsDeleted(flowId, executionName);
            return null;
        });
    }

    @Override
    public void checkExecutionIsActive(String flowId, String executionName) {
        retryManager.executeWithRetries(() -> {
            super.checkExecutionIsActive(flowId, executionName);
            return null;
        });
    }

    @Override
    public void checkExecutionIsInactive(String flowId, String executionName) {
        retryManager.executeWithRetries(() -> {
            super.checkExecutionIsInactive(flowId, executionName);
            return null;
        });
    }

    @Override
    public void checkExecutionState(FlowExecution flowExecution, String expectedState) {
        retryManager.executeWithRetries(() -> {
            super.checkExecutionState(flowExecution, expectedState);
            return null;
        });
    }

    @Override
    public void checkExecutionSummaryReport(FlowExecution flowExecution, String summaryReport, boolean outputToConsole) {
        retryManager.executeWithRetries(() -> {
            super.checkExecutionSummaryReport(flowExecution, summaryReport, outputToConsole);
            return null;
        });
    }

    @Override
    public void checkExecutionEventIsRecorded(FlowExecution execution, FlowExecutionEventSeverity severity, String message, String target) {
        retryManager.executeWithRetries(() -> {
            super.checkExecutionEventIsRecorded(execution, severity, message, target);
            return null;
        });
    }

    @Override
    public void checkExecutionInput(FlowExecution flowExecution, AbstractFlowInputCheckBuilder checkBuilder) {
        retryManager.executeWithRetries(() -> {
            super.checkExecutionInput(flowExecution, checkBuilder);
            return null;
        });
    }

    @Override
    public void checkFlowInputSchemaForFlowVersion(FlowDefinition flowDefinition, String flowVersion, String expectedSchema) {
        retryManager.executeWithRetries(() -> {
            super.checkFlowInputSchemaForFlowVersion(flowDefinition, flowVersion, expectedSchema);
            return null;
        });
    }

    @Override
    public void checkFlowDefinition(String flowId, FlowDefinition expectedDefinition) {
        retryManager.executeWithRetries(() -> {
            super.checkFlowDefinition( flowId, expectedDefinition);
            return null;
        });
    }

    @Override
    public void checkProcessDetailsForFlowVersion(FlowDefinition flowDefinition, String flowVersion, FlowVersionProcessDetails expectedVersionProcessDetails) {
        retryManager.executeWithRetries(() -> {
            super.checkProcessDetailsForFlowVersion(flowDefinition, flowVersion, expectedVersionProcessDetails);
            return null;
        });
    }

    @Override
    public void checkFlowVersionIsActive(String flowId, String flowVersion, boolean expectedIsActive) {
        retryManager.executeWithRetries(() -> {
            super.checkFlowVersionIsActive(flowId, flowVersion, expectedIsActive);
            return null;
        });
    }

    @Override
    public UserTask checkUsertaskActive(FlowExecution flowExecution, String usertaskName, boolean display) {
        return retryManager.executeWithRetries(() -> super.checkUsertaskActive(flowExecution, usertaskName, display));
    }
}
