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

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_DOES_NOT_SUPPORT_BACK;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_INVALID_BACK_TARGET;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.TASK_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

public abstract class BackUserTaskErrorTest extends FlowAutomationNegativeTest {
    // flow with backEnabled
    String FLOW_WITH_BACK_ENABLED = "flow-with-back-enabled-1.0.0";
    String FLOW_WITH_BACK_ENABLED_FLOW_ID = "com.ericsson.oss.fa.flows.backEnabled";

    // flow with no backEnabled
    String MULTI_INSTANCE_DOWNLOAD_VARIABLE = "multi-instance-download-variable-1.0.1";
    String MULTI_INSTANCE_DOWNLOAD_VARIABLE_FLOW_ID = "com.ericsson.oss.fa.flows.multiInstance.download-variables";

    FlowDefinition backEnabledflowDefinition;
    FlowDefinition multiInstanceDownloadVariableFlowDefinition;

    @Before
    public void before() {

        backEnabledflowDefinition = importFlow(FLOW_WITH_BACK_ENABLED_FLOW_ID,
                getFlowPackageBytes(FLOW_WITH_BACK_ENABLED));
        multiInstanceDownloadVariableFlowDefinition = importFlow(MULTI_INSTANCE_DOWNLOAD_VARIABLE_FLOW_ID,
                getFlowPackageBytes(MULTI_INSTANCE_DOWNLOAD_VARIABLE));
    }

    @Test
    public void test_backUserTask_TASK_NOT_FOUND () {
        String executionName = createUniqueInstanceName(FLOW_WITH_BACK_ENABLED_FLOW_ID);
        FlowExecution flowExecution = startFlowExecution(backEnabledflowDefinition, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);

        checkFlowFunctionFails(() ->
                        flowExecutionService.revertUserTask("InvalidTaskId"),
                TASK_NOT_FOUND);

    }

    @Test
    public void test_backUserTask_FLOW_EXECUTION_INVALID_BACK_TARGET () {
        String executionName = createUniqueInstanceName(FLOW_WITH_BACK_ENABLED_FLOW_ID);
        FlowExecution flowExecution = startFlowExecution(backEnabledflowDefinition, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);

        String userTaskId = checkUsertaskActive(flowExecution, "Information").getId();

        checkFlowFunctionFails(() ->
                        flowExecutionService.revertUserTask(userTaskId),
                FLOW_EXECUTION_INVALID_BACK_TARGET);

    }

    @Test
    public void test_backUserTask_FLOW_DOES_NOT_SUPPORT_BACK () {
        String executionName = createUniqueInstanceName(MULTI_INSTANCE_DOWNLOAD_VARIABLE_FLOW_ID);
        FlowExecution flowExecution = startFlowExecution(multiInstanceDownloadVariableFlowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        checkUsertaskActive(flowExecution, "Configuration");

        UsertaskInputBuilder usertaskInputBuilder = new UsertaskInputBuilder();
        usertaskInputBuilder.input("Configuration > Number of Elements", "" + 2);
        usertaskInputBuilder.input("Configuration > Load Control Pool Size", "2");
        usertaskInputBuilder.input("Configuration > Instance Sleep in Seconds", "1");
        usertaskInputBuilder.input("Configuration > Generate incidents", false);

        completeUsertask(flowExecution, "Configuration", usertaskInputBuilder);

        String userTaskId = checkUsertaskActive(flowExecution, "Upload File").getId();

        checkFlowFunctionFails(() ->
                        flowExecutionService.revertUserTask(userTaskId),
                FLOW_DOES_NOT_SUPPORT_BACK);

    }

    @After
    public void after() {
        removeFlow(FLOW_WITH_BACK_ENABLED_FLOW_ID);
        removeFlow(MULTI_INSTANCE_DOWNLOAD_VARIABLE_FLOW_ID);
    }
}
