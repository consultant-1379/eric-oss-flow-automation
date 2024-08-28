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

package com.ericsson.oss.services.flowautomation.flows.test;

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ReportSummaryValues.FAILED;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_HANDLING_INTERVAL;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;

public abstract class FlowThatFailsExecutionTest extends FlowAutomationBaseTest {

    String incidentHandlingflowPackage = "incident-handling";
    String incidentHandlingflowId = "com.ericsson.oss.fa.internal.flows.incidentHandling";
    FlowDefinition incidentHandlingFlowDefinition;
    FlowExecution incidentHandlingInstanceExecution = null;

    String flowThatFailsExecutionFlowPackage = "flow-that-fails-execution-1.0.0";
    String flowThatFailsExecutionFlowId = "com.ericsson.oss.fa.flows.flow-that-fails-execution";

    FlowDefinition flowThatFailsExecutionFlowDefinition;
    int numInitialFlowsImported;

    @Before
    public void before() {
        // If JSE test, import incidentHandling internal flow, If REST test, get existing incidentHandling Flow execution
        if (clientType == TestClientType.JSE)
        {
            System.setProperty(INCIDENT_HANDLING_INTERVAL, "5");
            incidentHandlingFlowDefinition = importFlow(incidentHandlingflowId, getFlowPackageBytes(incidentHandlingflowPackage));
            checkNumberOfExecutionsForFlow(incidentHandlingflowId, 0);
            incidentHandlingInstanceExecution = startFlowExecution(incidentHandlingFlowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(incidentHandlingflowId, incidentHandlingFlowDefinition.getFlowVersions().get(0).getVersion()));
            checkNumberOfExecutionsForFlow(incidentHandlingflowId, 1);
        }
        else {
            incidentHandlingInstanceExecution = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(incidentHandlingflowId).build()).get(0);
        }

        numInitialFlowsImported = getNumberFlowsImported();
        flowThatFailsExecutionFlowDefinition = importFlow(flowThatFailsExecutionFlowId,
                getFlowPackageBytes(flowThatFailsExecutionFlowPackage));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @Test
    public void testFlowThatFailsExecution() {

        String flowThatFailsExecutionExecName = createUniqueInstanceName(flowThatFailsExecutionFlowId);

        FlowExecution flowThatFailsExecution = startFlowExecution(flowThatFailsExecutionFlowDefinition,
                flowThatFailsExecutionExecName);

        checkExecutionIsActive(flowThatFailsExecution);

        checkExecutionFailed(flowThatFailsExecution);

        checkExecutionSummaryReport(flowThatFailsExecution, FAILED);
    }

    @After
    public void after() {
        removeFlow(flowThatFailsExecutionFlowId);

        // clean up internal flows
        if (clientType == TestClientType.JSE) {
            suspendExecution(incidentHandlingInstanceExecution);
            removeFlow(incidentHandlingflowId);
            checkNumberOfFlowsImported(0);
        } else if (clientType == TestClientType.REST) {
            checkNumberOfFlowsImported(numInitialFlowsImported);
        }
    }
}
