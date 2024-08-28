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

import static org.junit.Assert.assertEquals;

import static com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity.INFO;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

public abstract class DslSdkFlowExample extends FlowAutomationBaseTest {

    String flowPackage = "sdk-flow-example";
    String flowId = "com.ericsson.oss.fa.flows.sdk-flow-example";
    FlowDefinition flowDefinition;

    private static final String NODES_SELECTION_RADIO_NODES = "Nodes Selection > Radio Nodes";
    private static final String RADIONODE_1 = "RadioNode-1";
    private static final String RADIONODE_ISO_V1 = "RadioNode-ISO-v1";
    private static final String SOFTWARE_PACKAGE_SELECTION_SOFTWARE_PACAKAGES = "Software Package Selection > Software Packages";

    int numInitialFlowsImported;

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @Test
    public void testSdkFlowWithSetupInteractive() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        checkExecutionSummaryReport(flowExecution, "Getting Nodes List");
        checkExecutionEventIsRecorded(flowExecution, INFO, "Getting Nodes List", null);

        completeUsertask(flowExecution, "Nodes Selection", new UsertaskInputBuilder().
        input(NODES_SELECTION_RADIO_NODES, RADIONODE_1));


        checkExecutionEventIsRecorded(flowExecution, INFO, "Selected Nodes Validated", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "Getting Software Packages List", null);
        checkExecutionSummaryReport(flowExecution, "Getting Software Packages List");

        completeUsertask(flowExecution, "Software Package Selection", new UsertaskInputBuilder().
        input(SOFTWARE_PACKAGE_SELECTION_SOFTWARE_PACAKAGES, RADIONODE_ISO_V1));

        checkExecutionEventIsRecorded(flowExecution, INFO, "Software Package Validated", null);
        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
        check(NODES_SELECTION_RADIO_NODES, RADIONODE_1).
        check(SOFTWARE_PACKAGE_SELECTION_SOFTWARE_PACAKAGES, RADIONODE_ISO_V1));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        FlowExecutionResource reportSheet = downloadFlowExecutionReport(flowExecution);
        assertEquals(flowExecution.getName()+"-report.xlsx", reportSheet.getName());

        checkExecutionReport(flowExecution);

        checkReportTable(flowExecution, "executionReport");

        checkExecutionEventIsRecorded(flowExecution, INFO, "PreUpgrade Node Health Check Completed", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "PreUpgrade Licence Check Completed", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "PreUpgrade Backup Completed", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "Performing Node Upgrade", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "Validating Node Upgrade", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "PostUpgrade Node Health Check Completed", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "Node Upgraded Successfully", null);

    }


    @Test
    public void testSdkFlowWithSetupNonInteractive() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup.zip"));

        checkExecutionSummaryReport(flowExecution, "Flow is being executed in non-interacitve mode");

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
        check(NODES_SELECTION_RADIO_NODES, RADIONODE_1).
        check(SOFTWARE_PACKAGE_SELECTION_SOFTWARE_PACAKAGES, RADIONODE_ISO_V1));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        //Check Execution Report

        checkExecutionReport(flowExecution);

        checkReportTable(flowExecution, "executionReport");


        //Check Event Tab Data
        checkExecutionEventIsRecorded(flowExecution, INFO, "PreUpgrade Node Health Check Completed", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "PreUpgrade Licence Check Completed", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "PreUpgrade Backup Completed", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "Performing Node Upgrade", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "Validating Node Upgrade", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "PostUpgrade Node Health Check Completed", null);
        checkExecutionEventIsRecorded(flowExecution, INFO, "Node Upgraded Successfully", null);
    }

    @After
    public void after() {
        removeFlow(flowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }

}
