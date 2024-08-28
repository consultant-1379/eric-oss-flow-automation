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

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_VALIDATION_FAILED;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class GetExecutionReportErrorTest extends FlowAutomationNegativeTest {

    private static final String flowPackageBaseFlow = "import-base-flow-1.0.1";
    private static final String flowIdBaseFlow = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow";

    private static final String flowPackageMultiInstanceDownloadVariable = "multi-instance-download-variable-1.0.1";
    private static final String flowIdMultiInstanceDownloadVariable = "com.ericsson.oss.fa.flows.multiInstance.download-variables";

    private static FlowDefinition flowDefinitionHelloWorld;
    private static FlowDefinition flowDefinitionMultiInstanceDownloadVariable;

    @Before
    public void before() {
        checkNumberOfExecutionsForFlow(flowIdBaseFlow, 0);
        checkNumberOfExecutionsForFlow(flowIdMultiInstanceDownloadVariable, 0);

        flowDefinitionHelloWorld = importFlow(flowIdBaseFlow, getFlowPackageBytes(flowPackageBaseFlow));

        flowDefinitionMultiInstanceDownloadVariable = importFlow(flowIdMultiInstanceDownloadVariable, getFlowPackageBytes(flowPackageMultiInstanceDownloadVariable));

    }

    @Test
    public void test_getExecutionReport_SCHEMA_NOT_FOUND() {
        final String executionName = createUniqueInstanceName(flowIdBaseFlow);
        final FlowExecution flowExecution = startFlowExecution(flowDefinitionHelloWorld, executionName);
        checkFlowFunctionFails(
                () -> flowExecutionService.getExecutionReport(flowExecution.getFlowId(), flowExecution.getName()),
                SCHEMA_NOT_FOUND);
    }

    @Test
    public void test_getExecutionReport_FLOW_EXECUTION_NOT_FOUND() {
        final String executionName = createUniqueInstanceName(flowIdBaseFlow);
        checkFlowFunctionFails(
                () -> flowExecutionService.getExecutionReport(flowIdBaseFlow, executionName),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @Test
    public void test_getExecutionReport_JSON_VALIDATION_FAILED() {
        final String executionName = createUniqueInstanceName(flowIdMultiInstanceDownloadVariable);
        final FlowExecution flowExecution = startFlowExecution(flowDefinitionMultiInstanceDownloadVariable, executionName);
        checkFlowFunctionFails(
                () -> flowExecutionService.getExecutionReport(flowExecution.getFlowId(), executionName),
                JSON_VALIDATION_FAILED);
    }


    @After
    public void after() {
        removeFlow(flowIdBaseFlow);
        removeFlow(flowIdMultiInstanceDownloadVariable);
    }

}
