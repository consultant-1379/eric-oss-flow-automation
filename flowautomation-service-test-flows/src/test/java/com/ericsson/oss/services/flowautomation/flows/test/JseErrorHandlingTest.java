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

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ReportSummaryValues.EXECUTE_FAILED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ReportSummaryValues.SETUP_FAILED;
import static com.ericsson.oss.services.flowautomation.flows.test.InternalFlowTestHelper.testInternalFlowHistory;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_HANDLING_INTERVAL;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Test cases for error handling flow.
 */
public class JseErrorHandlingTest extends FlowAutomationBaseTest {

    String flowPackage = "errorHandling-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.errorHandling";
    FlowDefinition flowDefinition;

    String incidentHandlingFlowPackage = "incident-handling";
    String incidentHandlingFlowId = "com.ericsson.oss.fa.internal.flows.incidentHandling";
    FlowExecution incidentHandlingInstanceExecution = null;

    @Before
    public void before() {
        System.setProperty(INCIDENT_HANDLING_INTERVAL, "5");
        flowDefinition = importFlow(incidentHandlingFlowId, getFlowPackageBytes(incidentHandlingFlowPackage));
        checkNumberOfFlowsImported(1);
        checkNumberOfExecutionsForFlow(incidentHandlingFlowId, 0);
        incidentHandlingInstanceExecution = startFlowExecution(flowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(incidentHandlingFlowId, flowDefinition.getFlowVersions().get(0).getVersion()));
        checkNumberOfExecutionsForFlow(incidentHandlingFlowId, 1);
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(2);
        testInternalFlowHistory(incidentHandlingInstanceExecution);
    }

    @After
    public void after() {
        removeFlow(flowId);
        removeFlow(incidentHandlingFlowId);
    }

    @Test
    public void testSuccess() {
        final FlowExecution flowExecution = startFlowExecution();
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Success", true));
        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        checkExecutionSummaryReport(flowExecution, "Level 1 success");
    }

    @Test
    public void testSetupError() {
        final FlowExecution flowExecution = startFlowExecution();
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Setup Error", true));

        checkExecutionFailedSetup(flowExecution);

        checkExecutionSummaryReport(flowExecution, SETUP_FAILED);
    }

    @Test
    public void testSetupIncident() {
        final FlowExecution flowExecution = startFlowExecution();
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Setup Incident", true));

        checkExecutionFailedSetup(flowExecution);

        checkExecutionSummaryReport(flowExecution, SETUP_FAILED);
    }

    @Test
    public void testExecuteLevel1Error() {
        final FlowExecution flowExecution = startFlowExecution();
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Execute Level 1 Error", true));
        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionFailedExecute(flowExecution);

        checkExecutionSummaryReport(flowExecution, EXECUTE_FAILED);
    }

    @Test
    public void testExecuteLevel1Incident() {
        final FlowExecution flowExecution = startFlowExecution();
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Execute Level 1 Incident", true));
        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionFailedExecute(flowExecution);

        checkExecutionSummaryReport(flowExecution, EXECUTE_FAILED);
    }

    @Test
    public void testExecuteLevel2Error() {
        final FlowExecution flowExecution = startFlowExecution();
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Execute Level 2 Error", true));
        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        checkExecutionSummaryReport(flowExecution, "Level 2 error caught at level 1");
    }

    @Test
    public void testExecuteLevel2Incident() {
        final FlowExecution flowExecution = startFlowExecution();
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Execute Level 2 Incident", true));
        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        checkExecutionSummaryReport(flowExecution, "Level 2 error caught at level 1");
    }

    @Test
    public void testExecuteLevel2ParallelIncidents() {
        final FlowExecution flowExecution = startFlowExecution();
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Parallel Paths", true).
                input("Configuration > Required Behaviour > Execute Level 2 Incident", true));
        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        checkExecutionSummaryReport(flowExecution, "Level 2 error caught at level 1");
    }


    @Test
    public void testExecuteLevel2IncidentMultipleProcessInstances() {
        final FlowExecution flowExecution1 = startFlowExecution();
        completeUsertask(flowExecution1, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Execute Level 2 Incident", true));
        completeUsertaskReviewAndConfirm(flowExecution1);

        final FlowExecution flowExecution2 = startFlowExecution();
        completeUsertask(flowExecution2, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Execute Level 2 Incident", true));
        completeUsertaskReviewAndConfirm(flowExecution2);

        checkExecutionExecuted(flowExecution1);
        checkExecutionExecuted(flowExecution2);

        checkExecutionSummaryReport(flowExecution1, "Level 2 error caught at level 1");
        checkExecutionSummaryReport(flowExecution2, "Level 2 error caught at level 1");
    }

    private FlowExecution startFlowExecution() {
        final String executionName = createUniqueInstanceName(flowId);
        final FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);
        return flowExecution;
    }

    @Override
    protected TestClientType selectClientType() {
        return TestClientType.JSE;
    }

}
