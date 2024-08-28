/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.interfacetest;

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_JSON_PAYLOAD;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.TASK_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CompleteUserTaskErrorTest extends FlowAutomationNegativeTest {

    private static final String SETUP_ZIP = "setup.zip";
    private static final String SETUP_ZIP_INVALID = "setup_invalid.zip";

    String flowPackage = "helloWorld-1.0.1";
    String FLOW_ID = "com.ericsson.oss.fa.flows.helloWorld";

    @Before
    public void before() {
        importFlow(FLOW_ID, getFlowPackageBytes(flowPackage));
    }


    @Test
    public void test_completeUserTask_TASK_NOT_FOUND() {
        String executionName = createUniqueInstanceName(FLOW_ID);
        flowService.executeFlow(FLOW_ID, executionName);

        List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(FLOW_ID).flowExecutionName(executionName).build());
        FlowExecution flowExecution = flowExecutions.get(0);

        checkUsertaskActive(flowExecution, "Choose Setup");

        Map<String, byte[]> userTaskFileInput = new HashMap<>();
        getFlowdataFileBytes(flowPackage, SETUP_ZIP);
        String userTaskInput = "{\"setupType\":{\"fileInput\":{\"fileName\":\"setup.zip\"}}}";

        checkFlowFunctionFails(() -> flowExecutionService.completeUserTask("taskNotFound", userTaskFileInput, userTaskInput), TASK_NOT_FOUND);

        checkFlowFunctionFails(() -> flowExecutionService.completeUserTask("taskNotFound", Collections.emptyMap()), TASK_NOT_FOUND);
    }

    @Test
    public void test_completeUserTask_INVALID_JSON_PAYLOAD() {

        String executionName = createUniqueInstanceName(FLOW_ID);
        flowService.executeFlow(FLOW_ID, executionName);
        List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(FLOW_ID).flowExecutionName(executionName).build());
        FlowExecution flowExecution = flowExecutions.get(0);

        checkUsertaskActive(flowExecution, "Choose Setup");

        // Get and execute 'Choose Setup' usertask
        List<UserTask> userTasks = flowExecutionService.getUserTasks(FLOW_ID, executionName);
        UserTask userTask = userTasks.get(0);

        Map<String, byte[]> userTaskFileInput = new HashMap<>();
        userTaskFileInput.put(SETUP_ZIP,  getFlowdataFileBytes(flowPackage, SETUP_ZIP_INVALID));
        String userTaskInput = "{\"setupType\":{\"fileInput\":{\"fileName\":\"setup_invalid.zip\"}}}";
            checkFlowFunctionFails(() -> flowExecutionService.completeUserTask(userTask.getId(), userTaskFileInput, userTaskInput), INVALID_JSON_PAYLOAD);
    }
    
    @After
    public void after() {
        removeFlow(FLOW_ID);
    }
}
