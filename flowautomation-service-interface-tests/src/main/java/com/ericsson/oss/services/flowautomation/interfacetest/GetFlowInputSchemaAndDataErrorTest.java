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
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.SETUP_PHASE_NOT_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public abstract class GetFlowInputSchemaAndDataErrorTest extends FlowAutomationNegativeTest {

    private static final String flowPackageWithoutSetup = "flow-without-setup-1.0.0";
    private static final String flowIdWithoutSetup = "com.ericsson.oss.fa.flows.without-setup";

    private static FlowDefinition flowDefinitionWithoutSetup;

    private int numInitialFlowsImported;

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        checkNumberOfExecutionsForFlow(flowIdWithoutSetup, 0);

        flowDefinitionWithoutSetup = importFlow(flowIdWithoutSetup, getFlowPackageBytes(flowPackageWithoutSetup));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @Test
    public void test_getFlowInputSchemaAndData_FLOW_EXECUTION_NOT_FOUND() {
        final String executionName = createUniqueInstanceName(flowIdWithoutSetup);
        final FlowExecution flowExecution = startFlowExecution(flowDefinitionWithoutSetup, executionName);
        checkFlowFunctionFails(
                () -> flowExecutionService.getFlowInputSchemaAndData(flowExecution.getFlowId(), "flowExecutionNoName"),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @Test
    public void test_getFlowInputSchemaAndData_SETUP_PHASE_NOT_AVAILABLE() {
        final String executionName = createUniqueInstanceName(flowIdWithoutSetup);
        final FlowExecution flowExecution = startFlowExecution(flowDefinitionWithoutSetup, executionName);
        checkFlowFunctionFails(
                () -> flowExecutionService.getFlowInputSchemaAndData(flowExecution.getFlowId(), executionName),
                SETUP_PHASE_NOT_AVAILABLE);
    }

    @After
    public void after() {
        removeFlow(flowIdWithoutSetup);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}
