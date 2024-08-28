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
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.NO_FLOW_EXECUTION_RESOURCE_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;
import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class GetExecutionReportVariableErrorTest extends FlowAutomationNegativeTest {

    public static final String INVALID_REPORT_VARIABLE = "noName";

    public static final String FLOW_PACKAGE = "multi-instance-download-variable-1.0.1";
    public static final String FLOW_ID = "com.ericsson.oss.fa.flows.multiInstance.download-variables";

    FlowDefinition flowDefinition;


    @Before
    public void before() {
        flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(FLOW_PACKAGE));
    }

    @Test
    public void test_getExecutionReportVariable_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.getExecutionReportVariable("NoFlow", "hw-test-1", INVALID_REPORT_VARIABLE),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_getExecutionReportVariable_FLOW_EXECUTION_NOT_FOUND() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.getExecutionReportVariable(FLOW_ID, "hw-test-1", INVALID_REPORT_VARIABLE),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @Test
    public void test_getExecutionReportVariable_NO_FLOW_EXECUTION_RESOURCE_AVAILABLE() {

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

        checkFlowFunctionFails(() ->
                        flowExecutionService.getExecutionReportVariable(FLOW_ID, executionName, INVALID_REPORT_VARIABLE),
                NO_FLOW_EXECUTION_RESOURCE_AVAILABLE);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }

}
