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

import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_DOES_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_VERSION_IS_ACTIVE;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_VERSION_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_VERSION_SYNTAX_INVALID;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;
import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;


public abstract class SuspendAllExecutionsForFlowWithVersionErrorTest extends FlowAutomationNegativeTest {
    String FLOW_PACKAGE = "multi-instance-download-variable-1.0.1";
    String FLOW_ID = "com.ericsson.oss.fa.flows.multiInstance.download-variables";
    String FLOW_VERSION = "1.0.5";

    FlowDefinition flowDefinition;

    @Before
    public void before() {
        flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(FLOW_PACKAGE));
    }

    @Test
    public void test_suspendAllExecutionsForFlowVersion_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.suspendAllExecutionsForFlowVersion("NoFlow", "flowVersion"),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_suspendAllExecutionsForFlowVersion_FLOW_VERSION_SYNTAX_INVALID() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.suspendAllExecutionsForFlowVersion(FLOW_ID, "1233.1233.123"),
                FLOW_VERSION_SYNTAX_INVALID);
    }

    @Test
    public void test_suspendAllExecutionsForFlowVersion_FLOW_VERSION_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.suspendAllExecutionsForFlowVersion(FLOW_ID, "1.0.12"),
                FLOW_VERSION_NOT_EXIST);
    }

    @Test
    public void test_suspendAllExecutionsForFlowVersion_FLOW_VERSION_IS_ACTIVE() {
        String executionName = createUniqueInstanceName(FLOW_ID);

        startFlowExecution(flowDefinition, executionName);

        checkFlowFunctionFails(() ->
                        flowExecutionService.suspendAllExecutionsForFlowVersion(FLOW_ID, FLOW_VERSION),
                FLOW_VERSION_IS_ACTIVE);

    }

    @Test
    public void test_suspendAllExecutionsForFlowVersion_INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND() {
        String executionName = createUniqueInstanceName(FLOW_ID);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        UsertaskInputBuilder usertaskInputBuilder = new UsertaskInputBuilder();
        usertaskInputBuilder.input("Configuration > Number of Elements", "" + 2);
        usertaskInputBuilder.input("Configuration > Load Control Pool Size", "2");
        usertaskInputBuilder.input("Configuration > Instance Sleep in Seconds", "1");
        usertaskInputBuilder.input("Configuration > Generate incidents", false);

        completeUsertask(flowExecution, "Configuration", usertaskInputBuilder);
        completeUsertask(flowExecution, "Upload File", new UsertaskInputBuilder().
                input("Upload File > File", "file.txt", getFlowdataFileBytes(FLOW_PACKAGE, "file.txt")));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        checkExecutionReport(flowExecution);
        // Check report
        String report = getExecutionReport(flowExecution);
        with(report).assertThat("$.header.status", equalTo("COMPLETED"));

        flowService.activateFlowVersion(FLOW_ID, FLOW_VERSION, false);

        checkFlowFunctionFails(() ->
                        flowExecutionService.suspendAllExecutionsForFlowVersion(FLOW_ID, FLOW_VERSION),
                INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND);

    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }

}