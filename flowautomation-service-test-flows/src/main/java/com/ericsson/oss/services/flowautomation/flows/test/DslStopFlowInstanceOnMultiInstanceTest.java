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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

public abstract class DslStopFlowInstanceOnMultiInstanceTest extends FlowAutomationBaseTest {

    String stopFlowInstanceflowPackage = "stop-flow-instance";
    String stopFlowInstanceflowId = "com.ericsson.oss.fa.internal.flows.stopflowinstance";

    String flowPackage = "multiInstance-loadControl-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.multiInstanceLoadControl";

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

        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(2);
    }

    @Test
    public void testEndToEndFlowExecution() {
        @SuppressWarnings({"squid:S00117"}) final int NUM_ELEMENTS = 5;

        final String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Number of Elements", "" + NUM_ELEMENTS).
                input("Configuration > Load Control Pool Size", "4").
                input("Configuration > Instance Sleep in Seconds", "10"));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check("Configuration > Number of Elements", "" + NUM_ELEMENTS).
                check("Configuration > Load Control Pool Size", "4").
                check("Configuration > Instance Sleep in Seconds", "10"));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionState(flowExecution, "EXECUTING");
        stopExecution(flowExecution);

        checkExecutionStopped(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Manually Stopped");
    }

    @After
    public void after() {
        suspendExecution(stopFlowInstanceExecution);
        removeFlow(flowId);
        removeFlow(stopFlowInstanceflowId);
        checkNumberOfFlowsImported(0);
    }
}
