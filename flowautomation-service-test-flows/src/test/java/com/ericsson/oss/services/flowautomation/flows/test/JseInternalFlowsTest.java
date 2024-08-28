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

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ReportSummaryValues.SETUP_FAILED;
import static com.ericsson.oss.services.flowautomation.flows.test.InternalFlowTestHelper.testInternalFlowHistory;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_HANDLING_INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INTERVAL_TIME_UNIT;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.REINDEX_PATH;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.RETENTION_PERIOD;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.RETENTION_PERIOD_TIME_UNIT;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.STOP_FLOW_INSTANCE_INTERVAL;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.copyFileFromResources;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.realSleepMs;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.setPosixFilePermissions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingIntervalTimeUnit;
import com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingRetentionPeriodTimeUnit;
import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * The type Jse internal flows test.
 */
public class JseInternalFlowsTest extends FlowAutomationBaseTest {
    @Override
    protected TestClientType selectClientType() {
        return TestClientType.JSE;
    }

    String houseKeepingflowPackage = "house-keeping";
    String houseKeepingflowId = "com.ericsson.oss.fa.internal.flows.houseKeeping";

    String incidentHandlingFlowPackage = "incident-handling";
    String incidentHandlingFlowId = "com.ericsson.oss.fa.internal.flows.incidentHandling";

    String stopFlowInstanceflowPackage = "stop-flow-instance";
    String stopFlowInstanceflowId = "com.ericsson.oss.fa.internal.flows.stopflowinstance";

    String flowPackageWithOutSetup = "flow-usertask-input-validation-1.0.1";
    String flowIdWithOutSetup = "com.ericsson.oss.fa.flows.usertaskvalidation";

    String flowPackageErrorHandling = "errorHandling-1.0.1";
    String flowIdErrorHandling = "com.ericsson.oss.fa.flows.errorHandling";

    FlowExecution houseKeepingExecution = null;
    FlowExecution stopFlowInstanceExecution = null;
    FlowExecution incidentHandlingInstanceExecution = null;
    FlowDefinition flowDefinition;

    String reindexPath;

    @Before
    public void before() {

        //House keeping runs every 10 seconds and cleans up data which are older than 10 seconds.
        System.setProperty(INTERVAL, "15");
        System.setProperty(INTERVAL_TIME_UNIT, HouseKeepingIntervalTimeUnit.SECOND.getValue());
        System.setProperty(RETENTION_PERIOD, "15");
        System.setProperty(RETENTION_PERIOD_TIME_UNIT, HouseKeepingRetentionPeriodTimeUnit.SECOND.getValue());

        reindexPath = copyFileFromResources("scripts/reindex.sh");
        if (reindexPath != null) {
            setPosixFilePermissions(reindexPath, "rwxr-xr-x");
            System.setProperty(REINDEX_PATH, reindexPath);
        }

        flowDefinition = importFlow(houseKeepingflowId, getFlowPackageBytes(houseKeepingflowPackage));
        checkNumberOfFlowsImported(1);
        checkNumberOfExecutionsForFlow(houseKeepingflowId, 0);
        houseKeepingExecution = startFlowExecution(flowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(houseKeepingflowId, flowDefinition.getFlowVersions().get(0).getVersion()));
        checkNumberOfExecutionsForFlow(houseKeepingflowId, 1);

        // Stop Flow instance runs every 5 seconds
        System.setProperty(STOP_FLOW_INSTANCE_INTERVAL, "5");
        flowDefinition = importFlow(stopFlowInstanceflowId, getFlowPackageBytes(stopFlowInstanceflowPackage));
        checkNumberOfFlowsImported(2);
        checkNumberOfExecutionsForFlow(stopFlowInstanceflowId, 0);
        stopFlowInstanceExecution = startFlowExecution(flowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(stopFlowInstanceflowId, flowDefinition.getFlowVersions().get(0).getVersion()));
        checkNumberOfExecutionsForFlow(stopFlowInstanceflowId, 1);

        // Incident Handler runs every 5 seconds
        System.setProperty(INCIDENT_HANDLING_INTERVAL, "5");
        flowDefinition = importFlow(incidentHandlingFlowId, getFlowPackageBytes(incidentHandlingFlowPackage));
        checkNumberOfFlowsImported(3);
        checkNumberOfExecutionsForFlow(incidentHandlingFlowId, 0);
        incidentHandlingInstanceExecution = startFlowExecution(flowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(incidentHandlingFlowId, flowDefinition.getFlowVersions().get(0).getVersion()));
        checkNumberOfExecutionsForFlow(incidentHandlingFlowId, 1);

        testInternalFlowHistory(houseKeepingExecution);
        testInternalFlowHistory(stopFlowInstanceExecution);
        testInternalFlowHistory(incidentHandlingInstanceExecution);
    }

    @Test
    public void testHouseKeepingAndStopFlowInstanceInExecutingPhase() {
        flowDefinition = importFlow(flowIdWithOutSetup, getFlowPackageBytes(flowPackageWithOutSetup));
        final String executionName = createUniqueInstanceName(flowIdWithOutSetup);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        checkNumberOfExecutionsForFlow(flowIdWithOutSetup, 1);

        checkExecutionState(flowExecution, "EXECUTING");

        stopExecution(flowExecution);

        checkExecutionStopped(flowExecution);

        int count = 0;
        while (!getExecutionsForFlow(flowIdWithOutSetup).isEmpty() && count < 18) {
            count++;
            realSleepMs(5000);
        }

        // this pause allows the REINDEX script to be fully executed and the report variables to be updated
        realSleepMs(3000);

        String index = getExecutionReportVariableContent(houseKeepingExecution, "reindexDateTimes");
        downloadAndCheckReportVariableContentEqualsTo(houseKeepingExecution, index + ".scriptExecutionOutput", "REINDEX of TABLE act_hi_actinst executed successfully.");
        downloadAndCheckReportVariableContentEqualsTo(houseKeepingExecution, index + ".reindexExecutionDuration", "3");
        downloadAndCheckReportVariableContentEqualsTo(houseKeepingExecution, index + ".tableInitialSize", "120.0");
        downloadAndCheckReportVariableContentEqualsTo(houseKeepingExecution, index + ".tableFinalSize", "0.11");

        checkNumberOfExecutionsForFlow(flowIdWithOutSetup, 0);
        removeFlow(flowIdWithOutSetup);
    }


    @Test
    public void testHouseKeepingAndIncidentHandlingSetupError() {
        flowDefinition = importFlow(flowIdErrorHandling, getFlowPackageBytes(flowPackageErrorHandling));
        final String executionName = createUniqueInstanceName(flowIdErrorHandling);
        final FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        checkNumberOfExecutionsForFlow(flowIdErrorHandling, 1);
        completeUsertaskChooseSetupInteractive(flowExecution);
        completeUsertask(flowExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Setup Error", true));

        checkExecutionFailedSetup(flowExecution);

        checkExecutionSummaryReport(flowExecution, SETUP_FAILED);

        checkNumberOfExecutionsForFlow(flowIdErrorHandling, 0);
        removeFlow(flowIdErrorHandling);
    }

    @After
    public void after() {
        suspendExecution(stopFlowInstanceExecution);
        removeFlow(stopFlowInstanceflowId);

        suspendExecution(incidentHandlingInstanceExecution);
        removeFlow(incidentHandlingFlowId);

        suspendExecution(houseKeepingExecution);
        removeFlow(houseKeepingflowId);

        if (reindexPath != null) {
            System.clearProperty(REINDEX_PATH);
        }
    }
}

