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
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class GetUserTasksErrorTest extends FlowAutomationNegativeTest {

    private static final String flowPackageWithoutSetup = "helloWorld-1.0.1";
    private static final String flowIdWithoutSetup = "com.ericsson.oss.fa.flows.helloWorld";

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
    public void test_getUserTasks_FLOW_DOES_NOT_EXIST() {
        final String executionName = createUniqueInstanceName(flowIdWithoutSetup);
        checkFlowFunctionFails(
                () -> flowExecutionService.getUserTasks("noFlowID", executionName),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_getUserTasks_FLOW_EXECUTION_NOT_FOUND() {
        final String executionName = createUniqueInstanceName(flowIdWithoutSetup);
        checkFlowFunctionFails(
                () -> flowExecutionService.getUserTasks(flowIdWithoutSetup, executionName),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @After
    public void after() {
        removeFlow(flowIdWithoutSetup);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }

}
