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

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.test.fwk.AbstractFlowInputCheckBuilder;

public interface FlowAutomationCheck {
    void checkNumberOfFlowsImported(int number);

    void checkNumberOfExecutions(int number);

    void checkNumberOfActiveExecutions(int number);

    void checkNumberOfExecutionsForFlow(String flowId, int number);

    void checkNumberOfActiveExecutionsForFlow(String flowId, int number);

    void checkExecutionIsDeleted(String flowId, String executionName);

    void checkExecutionIsActive(String flowId, String executionName);

    void checkExecutionIsInactive(String flowId, String executionName);

    void checkExecutionState(final FlowExecution flowExecution, String expectedState);

    void checkExecutionEventIsRecorded(FlowExecution execution, FlowExecutionEventSeverity severity, String message, String target);

    void checkExecutionInput(final FlowExecution flowExecution, AbstractFlowInputCheckBuilder checkBuilder);

    void checkFlowInputSchemaForFlowVersion(FlowDefinition flowDefinition, String flowVersion, String expectedSchema);

    void checkFlowDefinition(String flowId, FlowDefinition expectedDefinition);

    void checkProcessDetailsForFlowVersion(FlowDefinition flowDefinition, String flowVersion, FlowVersionProcessDetails expectedVersionProcessDetails);

    void checkFlowVersionIsActive(String flowId, String flowVersion, boolean expectedIsActive);

    UserTask checkUsertaskActive(FlowExecution flowExecution, String usertaskName, boolean display);

    void checkExecutionSummaryReport(FlowExecution flowExecution, String summaryReport, boolean outputToConsole);
}