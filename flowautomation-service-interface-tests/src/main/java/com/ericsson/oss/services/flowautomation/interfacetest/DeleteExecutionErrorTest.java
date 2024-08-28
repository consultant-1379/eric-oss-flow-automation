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
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_EXECUTION_DELETE;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;

public abstract class DeleteExecutionErrorTest extends FlowAutomationNegativeTest {

    private static final String FLOW_PACKAGE = "import-base-flow-1.0.1";
    private static final String FLOW_ID = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow";

    private static final String FLOW_PACKAGE_WITHOUT_SETUP = "stop-flow-instance-1.0.0";
    private static final String FLOW_ID_WITHOUT_SETUP = "com.ericsson.oss.fa.flows.stop.flow.instance";

    private FlowDefinition flowDefinition;

    private static final String FLOW_EXECUTION_NAME = "flowExecutionName";

    @Before
    public void before() {
        flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(FLOW_PACKAGE));
    }

    @Test
    public void test_deleteExecution_FLOW_EXECUTION_NOT_FOUND() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.deleteExecution(FLOW_ID, FLOW_EXECUTION_NAME),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @Test
    public void test_deleteExecution_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.deleteExecution("invalidFlowId", "flowExecutionName"),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_deleteExecution_INVALID_FLOW_EXECUTION_DELETE() {
        flowDefinition = importFlow(FLOW_ID_WITHOUT_SETUP, getFlowPackageBytes(FLOW_PACKAGE_WITHOUT_SETUP));

        final String executionName = createUniqueInstanceName(FLOW_ID_WITHOUT_SETUP);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        checkExecutionState(flowExecution, "EXECUTING");
        stopExecution(flowExecution);

        checkFlowFunctionFails(() ->
                        flowExecutionService.deleteExecution(FLOW_ID_WITHOUT_SETUP, executionName),
                INVALID_FLOW_EXECUTION_DELETE);

        removeFlow(FLOW_ID_WITHOUT_SETUP);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }

}
