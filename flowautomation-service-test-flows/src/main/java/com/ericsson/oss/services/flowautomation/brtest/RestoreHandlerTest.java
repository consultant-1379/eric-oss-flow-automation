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

package com.ericsson.oss.services.flowautomation.brtest;

import com.ericsson.oss.services.flowautomation.brapi.RestoreHandler;
import com.ericsson.oss.services.flowautomation.brtest.fwk.jse.InternalTestContext;
import com.ericsson.oss.services.flowautomation.brtest.fwk.rest.TestRestoreHandlerRest;

import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ReportSummaryValues.EXECUTE_FAILED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ReportSummaryValues.FAILED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ReportSummaryValues.SETUP_FAILED;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_HANDLING_INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.STOP_FLOW_INSTANCE_INTERVAL;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.realSleepMs;
import static com.ericsson.oss.services.flowautomation.test.fwk.rest.FlowAutomationUsers.admin;
import static org.junit.Assert.fail;


public abstract class RestoreHandlerTest extends FlowAutomationBaseTest {

    private static final String RESTORED_SUMMARY_REPORT_VALUE = "Restored";

    private RestoreHandler restoreHandler;
    private AtomicReference<String> securityContext = new AtomicReference<>(admin());

    String stopFlowInstanceflowPackage = "stop-flow-instance";
    String stopFlowInstanceflowId = "com.ericsson.oss.fa.internal.flows.stopflowinstance";
    FlowDefinition stopFlowInstanceFlowDefinition;
    FlowExecution stopFlowInstanceExecution = null;

    String incidentHandlingflowPackage = "incident-handling";
    String incidentHandlingflowId = "com.ericsson.oss.fa.internal.flows.incidentHandling";
    FlowDefinition incidentHandlingFlowDefinition;
    FlowExecution incidentHandlingInstanceExecution = null;

    String houseKeepingflowId = "com.ericsson.oss.fa.internal.flows.houseKeeping";
    FlowExecution houseKeepingInstanceExecution = null;

    String flowId = "com.ericsson.oss.fa.flows.restore-tester";
    String flowPackage = "flow-restore-tester-1.0.0";

    String helloWorldFlowPackage = "helloWorld-1.0.1";
    String helloWorldFlowId = "com.ericsson.oss.fa.flows.helloWorld";

    // Flow to verify that executions in FAILED_EXECUTE and FAILED_SETUP states are
    // ignored during suspension of executions after restore
    String errorHandlingFlowPackage = "errorHandling-1.0.1";
    String errorHandlingFlowId = "com.ericsson.oss.fa.flows.errorHandling";

    String flowThatFailsExecutionFlowPackage = "flow-that-fails-execution-1.0.0";
    String flowThatFailsExecutionFlowId = "com.ericsson.oss.fa.flows.flow-that-fails-execution";

    @Before
    public void before() throws InterruptedException {
        clientType = selectClientType();
        if (clientType == TestClientType.JSE)
        {
            restoreHandler = InternalTestContext.getInstance().getRestoreHandlerClient();
            System.setProperty(STOP_FLOW_INSTANCE_INTERVAL, "10");
            System.setProperty(INCIDENT_HANDLING_INTERVAL, "5");
            stopFlowInstanceFlowDefinition = importFlow(stopFlowInstanceflowId, getFlowPackageBytes(stopFlowInstanceflowPackage));
            incidentHandlingFlowDefinition = importFlow(incidentHandlingflowId, getFlowPackageBytes(incidentHandlingflowPackage));
            checkNumberOfFlowsImported(2);
            checkNumberOfExecutionsForFlow(stopFlowInstanceflowId, 0);
            stopFlowInstanceExecution = startFlowExecution(stopFlowInstanceFlowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(stopFlowInstanceflowId, stopFlowInstanceFlowDefinition.getFlowVersions().get(0).getVersion()));
            checkNumberOfExecutionsForFlow(stopFlowInstanceflowId, 1);
            checkNumberOfExecutionsForFlow(incidentHandlingflowId, 0);
            incidentHandlingInstanceExecution = startFlowExecution(incidentHandlingFlowDefinition, FlowAutomationInternalFlows.getFlowExecutionName(incidentHandlingflowId, incidentHandlingFlowDefinition.getFlowVersions().get(0).getVersion()));
            checkNumberOfExecutionsForFlow(incidentHandlingflowId, 1);
            // no housekeeping flow in JSE test because it fails to run on Windows.
        }
        else {
            restoreHandler = new TestRestoreHandlerRest(securityContext);
            stopFlowInstanceExecution = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(stopFlowInstanceflowId).build()).get(0);
            incidentHandlingInstanceExecution = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(incidentHandlingflowId).build()).get(0);
            houseKeepingInstanceExecution = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(houseKeepingflowId).build()).get(0);
        }
    }

    @Test
    public void testEndToEndServiceRestore() {
        /*
        * The following is tested in this method:
        * 1. restore-process-tester flow to verify that flow is not executing when process engine disabled &
        *    that it is suspended after the post-restore stage
        * 2. A flow execution in EXECUTED state should not get suspended
        * 3. A flow execution in FAILED, FAILED_SETUP or FAILED_EXECUTE state should not get suspended
        * 4. Internal flow executions should not get suspended
        * */

        // check if all internal flow instances are active
        checkExecutionIsActive(stopFlowInstanceflowId, stopFlowInstanceExecution.getName());
        checkExecutionIsActive(incidentHandlingflowId, incidentHandlingInstanceExecution.getName());
        if (clientType == TestClientType.REST) checkExecutionIsActive(houseKeepingflowId, houseKeepingInstanceExecution.getName());

        String executionName = createUniqueInstanceName(flowId);
        FlowDefinition flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));

        String helloWorldExecutionName = createUniqueInstanceName(helloWorldFlowId);
        FlowDefinition helloWorldFlowDefinition = importFlow(helloWorldFlowId, getFlowPackageBytes(helloWorldFlowPackage));

        FlowDefinition flowThatFailsExecutionFlowDefinition = importFlow(flowThatFailsExecutionFlowId, getFlowPackageBytes(flowThatFailsExecutionFlowPackage));

        FlowDefinition errorHandlingFlowDefinition = importFlow(errorHandlingFlowId, getFlowPackageBytes(errorHandlingFlowPackage));

        // Flow execution in FAILED state
        String flowThatFailsExecutionExecName = createUniqueInstanceName(flowThatFailsExecutionFlowId);
        FlowExecution flowThatFailsExecution = startFlowExecution(flowThatFailsExecutionFlowDefinition,
                flowThatFailsExecutionExecName);
        checkExecutionIsActive(flowThatFailsExecution);
        checkExecutionFailed(flowThatFailsExecution);
        checkExecutionSummaryReport(flowThatFailsExecution, FAILED);

        // Flow execution in FAILED_SETUP state
        String errorHandlingExecNameThatFailsSetup = createUniqueInstanceName(errorHandlingFlowId);
        FlowExecution flowExecutionThatFailsSetup = startFlowExecution(errorHandlingFlowDefinition,
                errorHandlingExecNameThatFailsSetup);
        completeUsertaskChooseSetupInteractive(flowExecutionThatFailsSetup);
        checkExecutionIsActive(flowExecutionThatFailsSetup);
        checkUsertaskActive(flowExecutionThatFailsSetup, "Configuration");
        completeUsertask(flowExecutionThatFailsSetup, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Setup Error", true));
        checkExecutionFailedSetup(flowExecutionThatFailsSetup);
        checkExecutionSummaryReport(flowExecutionThatFailsSetup, SETUP_FAILED);

        // Flow execution in FAILED_EXECUTE state
        String errorHandlingExecNameThatFailsExecution = createUniqueInstanceName(errorHandlingFlowId);
        FlowExecution flowExecutionThatFailsExecution = startFlowExecution(errorHandlingFlowDefinition,
                errorHandlingExecNameThatFailsExecution);
        completeUsertaskChooseSetupInteractive(flowExecutionThatFailsExecution);
        completeUsertask(flowExecutionThatFailsExecution, "Configuration", new UsertaskInputBuilder().
                input("Configuration > Required Behaviour > Execute Level 1 Error", true));
        completeUsertaskReviewAndConfirm(flowExecutionThatFailsExecution);
        checkExecutionFailedExecute(flowExecutionThatFailsExecution);
        checkExecutionSummaryReport(flowExecutionThatFailsExecution, EXECUTE_FAILED);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        FlowExecution helloWorldFlowExecution = startFlowExecution(helloWorldFlowDefinition, helloWorldExecutionName);

        checkExecutionIsActive(flowId, executionName);

        checkExecutionIsActive(helloWorldFlowId, helloWorldExecutionName);

        completeUsertaskChooseSetupFile(helloWorldFlowExecution, "flow-input.json", getFlowdataFileBytes(helloWorldFlowPackage, "flowInput.json"));

        completeUsertaskReviewAndConfirm(helloWorldFlowExecution);

        checkExecutionIsInactive(helloWorldFlowId, helloWorldExecutionName);

        checkExecutionExecuted(helloWorldFlowExecution);

        // Check if user task is active
        checkUsertaskActive(flowExecution, "Control Task");

        // Complete control user task to start timer
        completeUsertask(flowExecution, "Control Task", new UsertaskInputBuilder());

        // Start restore process
        restoreHandler.preRestoreActions();

        restoreHandler.preRestoreActions();

        // Starting a timer by which the flow should reach suspend task
        realSleepMs(25000);
        int noOfActiveUserTasks = getActiveUsertasksForExecution(flowExecution).size();
        if (noOfActiveUserTasks != 0) fail();

        restoreHandler.postRestoreActions();

        restoreHandler.postRestoreActions();

        checkExecutionExecuted(helloWorldFlowExecution);
        checkExecutionSuspended(flowExecution);
        checkExecutionSummaryReport(flowExecution, RESTORED_SUMMARY_REPORT_VALUE);

        // checks to verify that flow execution in FAILED_EXECUTE state remains unchanged after restore
        checkExecutionFailedExecute(flowExecutionThatFailsExecution);
        checkExecutionSummaryReport(flowExecutionThatFailsExecution, EXECUTE_FAILED);

        // checks to verify that flow execution in FAILED_SETUP state remains unchanged after restore
        checkExecutionFailedSetup(flowExecutionThatFailsSetup);
        checkExecutionSummaryReport(flowExecutionThatFailsSetup, SETUP_FAILED);

        // checks to verify that flow execution in FAILED state remains unchanged after restore
        checkExecutionFailed(flowThatFailsExecution);
        checkExecutionSummaryReport(flowThatFailsExecution, FAILED);

        // Assert that all internal flow instances are active after restore process is finished
        checkExecutionIsActive(stopFlowInstanceflowId, stopFlowInstanceExecution.getName());
        checkExecutionIsActive(incidentHandlingflowId, incidentHandlingInstanceExecution.getName());
        if (clientType == TestClientType.REST) checkExecutionIsActive(houseKeepingflowId, houseKeepingInstanceExecution.getName());

    }

    @After
    public void after() {
        // clean up custom flows imported during the tests
        removeFlow(flowId);
        removeFlow(helloWorldFlowId);
        removeFlow(errorHandlingFlowId);
        removeFlow(flowThatFailsExecutionFlowId);

        // clean up internal flows
        if (clientType == TestClientType.JSE) {
            suspendExecution(stopFlowInstanceExecution);
            removeFlow(stopFlowInstanceflowId);
            suspendExecution(incidentHandlingInstanceExecution);
            removeFlow(incidentHandlingflowId);
            checkNumberOfFlowsImported(0);
        } else {
            checkNumberOfFlowsImported(3);
        }
    }
}