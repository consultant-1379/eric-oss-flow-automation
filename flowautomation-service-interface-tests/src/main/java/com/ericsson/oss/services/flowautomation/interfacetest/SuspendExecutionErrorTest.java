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
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.*;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;

public abstract class SuspendExecutionErrorTest extends FlowAutomationNegativeTest{

    String flowPackage = "helloWorld-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.helloWorld";

    FlowDefinition flowDefinition;

    @Before
    public void before() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @Test
    public void test_suspendExecution_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.suspendExecution("NoFlow", "flowName"),
                FLOW_DOES_NOT_EXIST);
    }


    @Test
    public void test_suspendExecution_FLOW_EXECUTION_NOT_FOUND() {
        String executionName = createUniqueInstanceName(flowId);

        startFlowExecution(flowDefinition, executionName);

        checkFlowFunctionFails(() ->
                        flowExecutionService.suspendExecution(flowId, "Hello World"),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @Test
    public void test_suspendExecution_INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND() {
        String executionName = createUniqueInstanceName(flowId);

        startFlowExecution(flowDefinition, executionName);

        flowExecutionService.suspendExecution(flowId, executionName);

        checkFlowFunctionFails(() ->
                        flowExecutionService.suspendExecution(flowId, executionName),
                INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND);
    }

    @After
    public void after() {
        removeFlow(flowId);
    }
}