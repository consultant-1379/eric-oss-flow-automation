/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

package com.ericsson.oss.services.flowautomation.flows.test;

import static com.ericsson.oss.services.flowautomation.flows.test.InternalFlowTestHelper.testInternalFlowHistory;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.STOP_FLOW_INSTANCE_INTERVAL;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

public abstract class DslStopFlowInstanceOnExecutePhaseTest extends FlowAutomationBaseTest {

    String stopFlowInstanceflowPackage = "stop-flow-instance";
    String stopFlowInstanceflowId = "com.ericsson.oss.fa.internal.flows.stopflowinstance";

    String flowPackageWithOutSetup = "flow-usertask-input-validation-1.0.1";
    String flowIdWithOutSetup = "com.ericsson.oss.fa.flows.usertaskvalidation";

    FlowDefinition flowDefinition;
    FlowExecution stopFlowInstanceExecution = null;

    @Before
    public void before() {
        System.setProperty(STOP_FLOW_INSTANCE_INTERVAL, "10");
        flowDefinition = importFlow(stopFlowInstanceflowId, getFlowPackageBytes(stopFlowInstanceflowPackage));
        checkNumberOfFlowsImported(1);
        checkNumberOfExecutionsForFlow(stopFlowInstanceflowId, 0);
        stopFlowInstanceExecution = startFlowExecution(flowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(stopFlowInstanceflowId, flowDefinition.getFlowVersions().get(0).getVersion()));
        checkNumberOfExecutionsForFlow(stopFlowInstanceflowId, 1);
        testInternalFlowHistory(stopFlowInstanceExecution);
    }


    @Test
    public void testStopFlowInstanceInExecutingPhase() {
        flowDefinition = importFlow(flowIdWithOutSetup, getFlowPackageBytes(flowPackageWithOutSetup));
        checkNumberOfFlowsImported(2);
        final String executionName = createUniqueInstanceName(flowIdWithOutSetup);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        checkNumberOfExecutionsForFlow(flowIdWithOutSetup, 1);
        checkExecutionState(flowExecution, "EXECUTING");
        stopExecution(flowExecution);

        checkExecutionStopped(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Manually Stopped");

        removeFlow(flowIdWithOutSetup);
    }

    @After
    public void after() {
        suspendExecution(stopFlowInstanceExecution);
        removeFlow(stopFlowInstanceflowId);
        checkNumberOfFlowsImported(0);
    }
}
