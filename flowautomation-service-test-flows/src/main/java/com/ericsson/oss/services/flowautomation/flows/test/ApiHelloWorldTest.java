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

package com.ericsson.oss.services.flowautomation.flows.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static com.ericsson.oss.services.flowautomation.entities.FlowEntityStatus.ENABLED;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

/**
 * Example of a test using Flow Automation Service API.
 */
public abstract class ApiHelloWorldTest extends FlowAutomationBaseTest {

    String flowPackage = "helloWorld-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.helloWorld";

    private static final String SETUP_ZIP = "setup.zip";

    @Test
    public void testEndToEndFlowExecution() {

        String executionName = createUniqueInstanceName(flowId);

        int numInitialFlowsDeployed = flowService.getFlows().size();

        // Import flow
        byte[] packageBytes = getFlowPackageBytes(flowPackage);
        FlowDefinition flowDefinition = flowService.importFlow(packageBytes);
        assertEquals(flowId, flowDefinition.getId());
        assertEquals(ENABLED.getStatus(), flowDefinition.getStatus());

        // Query flows - 1 more deployed
        List<FlowDefinition> flowDefinitions = flowService.getFlows();
        assertEquals(numInitialFlowsDeployed + 1L, flowDefinitions.size());

        // Execute flow
        flowService.executeFlow(flowId, executionName);
        List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowExecutionName(executionName).build());
        assertEquals(1, flowExecutions.size());
        FlowExecution flowExecution = flowExecutions.get(0);
        assertNull(flowExecution.getEndTime());

        checkUsertaskActive(flowExecution, "Choose Setup");

        // Get and execute 'Choose Setup' usertask
        List<UserTask> userTasks = flowExecutionService.getUserTasks(flowId, executionName);
        assertEquals(1, userTasks.size());
        UserTask userTask = userTasks.get(0);
        assertEquals("Choose Setup", userTask.getName());

        Map<String, byte[]> userTaskFileInput = new HashMap<>();
        getFlowdataFileBytes(flowPackage, SETUP_ZIP);
        userTaskFileInput.put(SETUP_ZIP,  getFlowdataFileBytes(flowPackage, SETUP_ZIP));
        String userTaskInput = "{\"setupType\":{\"fileInput\":{\"fileName\":\"setup.zip\"}}}";
        flowExecutionService.completeUserTask(userTask.getId(), userTaskFileInput, userTaskInput);

        checkUsertaskActive(flowExecution, "Review and Confirm Execute");

        // Get and execution 'Review and Confirm Execute' usertask
        userTasks = flowExecutionService.getUserTasks(flowId, executionName);
        assertEquals(2, userTasks.size());
        userTask = userTasks.get(1);
        assertEquals("Review and Confirm Execute", userTask.getName());

        flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowExecutionName(executionName).build());
        assertEquals(1, flowExecutions.size());
        assertEquals("Setting up", flowExecutions.get(0).getSummaryReport());

        flowExecutionService.completeUserTask(userTask.getId(), null);

        checkExecutionSummaryReport(flowExecution, "Executed");
        checkExecutionExecuted(flowExecution);
        flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowExecutionName(executionName).build());
        assertEquals(1, flowExecutions.size());
        /* Idun-2141 */
        // assertNotNull(flowExecutions.get(0).getEndTime());  // This assertion was failing silently
        assertEquals("Executed", flowExecutions.get(0).getSummaryReport());
        assertNotNull("Failed to get endTime from flow Executions", flowExecutions.get(0).getEndTime());

        removeFlow(flowId);
        flowDefinitions = flowService.getFlows();
        assertEquals(numInitialFlowsDeployed, flowDefinitions.size());
    }
}
