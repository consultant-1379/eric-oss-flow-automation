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
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_EXECUTION_PHASE_FOR_STOP;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class StopExecutionErrorTest extends FlowAutomationNegativeTest {

    String FLOW_ID_IMPORT_BASE_FLOW_1_0_1 = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow";
    String IMPORT_BASE_FLOW_1_0_1 = "import-base-flow-1.0.1";
    String IMPORT_HELLO_WORLD = "helloWorld-1.0.1";
    String FLOW_ID_IMPORT_HELLO_WORLD = "com.ericsson.oss.fa.flows.helloWorld";

    @Before
    public void before() {
        importFlow(FLOW_ID_IMPORT_BASE_FLOW_1_0_1, getFlowPackageBytes(IMPORT_BASE_FLOW_1_0_1));
    }

    @Test
    public void test_stopExecution_FLOW_EXECUTION_NOT_FOUND() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.stopExecution(FLOW_ID_IMPORT_BASE_FLOW_1_0_1, "NoFlowExecution"),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @Test
    public void test_stopExecution_INVALID_FLOW_EXECUTION_PHASE_FOR_STOP() {
        FlowDefinition flowDefinition = importFlow(FLOW_ID_IMPORT_HELLO_WORLD, getFlowPackageBytes(IMPORT_HELLO_WORLD));
        String executionName = createUniqueInstanceName(FLOW_ID_IMPORT_HELLO_WORLD);
        startFlowExecution(flowDefinition, executionName);
        checkFlowFunctionFails(() ->
                        flowExecutionService.stopExecution(FLOW_ID_IMPORT_HELLO_WORLD, executionName),
                INVALID_FLOW_EXECUTION_PHASE_FOR_STOP);
        removeFlow(FLOW_ID_IMPORT_HELLO_WORLD);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID_IMPORT_BASE_FLOW_1_0_1);
    }

}