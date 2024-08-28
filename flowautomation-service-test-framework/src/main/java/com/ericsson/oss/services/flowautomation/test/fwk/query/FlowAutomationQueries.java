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
package com.ericsson.oss.services.flowautomation.test.fwk.query;

import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.UserTask;

import java.util.List;

public interface FlowAutomationQueries {
    int getNumberFlowsImported();

    List<FlowExecution> getActiveExecutions();

    List<FlowExecution> getActiveExecutionsForFlow(String flowId);

    List<FlowExecution> getExecutionsForFlow(String flowId);

    List<UserTask> getActiveUsertasksForExecution(FlowExecution flowExecution);

    List<UserTask> getAllUsertasksForExecution(FlowExecution flowExecution);

    List<UserTask> getUserTasks(FlowExecution flowExecution, String usertaskName);

    String getExecutionState(FlowExecution flowExecution);

    String getExecutionInput(FlowExecution flowExecution);
}
