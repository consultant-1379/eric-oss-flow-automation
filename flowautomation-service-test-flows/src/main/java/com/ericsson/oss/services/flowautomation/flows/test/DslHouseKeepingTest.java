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
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INTERVAL_TIME_UNIT;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.REINDEX_PATH;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.RETENTION_PERIOD;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.RETENTION_PERIOD_TIME_UNIT;
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

/**
 * The Class DslHouseKeepingTest.
 */
public abstract class DslHouseKeepingTest extends FlowAutomationBaseTest {

    String houseKeepingflowPackage = "house-keeping";
    String houseKeepingflowId = "com.ericsson.oss.fa.internal.flows.houseKeeping";

    String flowPackage = "flow-without-setup-1.0.0";
    String flowId = "com.ericsson.oss.fa.flows.without-setup";
    FlowDefinition flowDefinition;

    int numInitialFlowsImported;
    String reindexPath;

    FlowExecution houseKeepingExecution = null;

    @Before
    public void before() {
        //Housekeeping runs every 15 seconds and cleans up data which are older than 15 seconds.
        System.setProperty(INTERVAL, "15");
        System.setProperty(INTERVAL_TIME_UNIT, HouseKeepingIntervalTimeUnit.SECOND.getValue());
        System.setProperty(RETENTION_PERIOD, "15");
        System.setProperty(RETENTION_PERIOD_TIME_UNIT, HouseKeepingRetentionPeriodTimeUnit.SECOND.getValue());

        reindexPath = copyFileFromResources("scripts/reindex.sh");
        if (reindexPath != null) {
            setPosixFilePermissions(reindexPath, "rwxr-xr-x");
            System.setProperty(REINDEX_PATH, reindexPath);
        }

        flowDefinition = importFlow(houseKeepingflowId, getFlowPackageBytes("internalFlows", houseKeepingflowPackage));
        numInitialFlowsImported = getNumberFlowsImported();
        checkNumberOfExecutionsForFlow(houseKeepingflowId, 0);
        //need to pass latest version
        houseKeepingExecution = startFlowExecution(flowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(houseKeepingflowId, flowDefinition.getFlowVersions().get(0).getVersion()));
        checkNumberOfExecutionsForFlow(houseKeepingflowId, 1);
        testInternalFlowHistory(houseKeepingExecution);
    }

    @Test
    @SuppressWarnings("squid:S1155")
    public void testHouseKeeping() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        numInitialFlowsImported = getNumberFlowsImported();
        checkNumberOfFlowsImported(numInitialFlowsImported);
        checkIfNullExecutionReportVariable(houseKeepingExecution, "reindexDateTimes");

        String executionName = createUniqueInstanceName(flowId);
        startFlowExecution(flowDefinition, executionName);
        checkNumberOfExecutionsForFlow(flowId, 1);

        executionName = createUniqueInstanceName(flowId);
        startFlowExecution(flowDefinition, executionName);
        checkNumberOfExecutionsForFlow(flowId, 2);

        checkNumberOfActiveExecutionsForFlow(flowId, 0);

        int count = 0;
        while (!getExecutionsForFlow(flowId).isEmpty() && count < 18) {
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

        checkNumberOfExecutionsForFlow(flowId, 0);

        removeFlow(flowId);
    }

    @After
    public void after() {
        suspendExecution(houseKeepingExecution);
        removeFlow(houseKeepingflowId);
        checkNumberOfFlowsImported(0);
        if (reindexPath != null) {
            System.clearProperty(REINDEX_PATH);
        }
    }
}
