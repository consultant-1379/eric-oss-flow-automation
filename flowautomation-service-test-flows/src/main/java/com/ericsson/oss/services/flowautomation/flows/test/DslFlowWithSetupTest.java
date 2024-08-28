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

import static com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity.ERROR;
import static com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity.INFO;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEvent;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.model.PaginatedData;
import com.ericsson.oss.services.flowautomation.model.SortData;
import com.ericsson.oss.services.flowautomation.model.SortOrder;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

import java.util.Date;
import java.util.List;

/**
 * Test cases for Flow with setup.
 *
 * Note that while this test uses the FAS JSE API, it can also work against the FAS REST API by simply supplying a REST client based implementation of
 * FAS API.
 */
public abstract class DslFlowWithSetupTest extends FlowAutomationBaseTest {

    String initialVersion = "1.0.0";
    String flowPackage = "flow-with-setup-1.0.0";
    String flowId = "com.ericsson.oss.fa.flows.with-setup";
    FlowDefinition flowDefinition;

    int numInitialFlowsImported;

    private static final String FLOW_INPUT_JSON = "flowInput.json";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @Test
    public void testFlowWithSetupInteractive() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionIsInactive(flowId, executionName);

        checkExecutionEventIsRecorded(flowExecution, INFO, "Info event with message, test target and event data", "test-node1");
        checkExecutionEventIsRecorded(flowExecution, ERROR, "Error event with message only", null);

        deleteExecution(flowId,executionName);

        checkExecutionIsDeleted(flowId,executionName);
    }

    @Test
    public void testFlowWithSetupNonInteractive() {
        String executionName = createUniqueInstanceName(flowId);
        startFlowExecutionWithFile(flowDefinition, executionName, FLOW_INPUT_JSON, getFlowdataFileBytes(flowPackage, FLOW_INPUT_JSON));

        checkExecutionIsInactive(flowId, executionName);
    }

    @Test
    public void testFlowWithSetupNonInteractiveError() {
        String executionName = createUniqueInstanceName(flowId);

        thrown.expect(FlowAutomationException.class);
        startFlowExecutionWithFile(flowDefinition, executionName, FLOW_INPUT_JSON, getFlowdataFileBytes(flowPackage, "invalid-flow-input.json"));

        checkExecutionIsInactive(flowId, executionName);
    }

    @Test
    public void testFlowWithSetupWithFile() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupFile(flowExecution, "validSetupData.zip", getFlowdataFileBytes(flowPackage, "validSetupData.zip"));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionIsInactive(flowId, executionName);
    }

    @Test
    public void testFlowWithSetupWithFileError() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        thrown.expect(FlowAutomationException.class);
        completeUsertaskChooseSetupFile(flowExecution, "invalidSetupData.zip", getFlowdataFileBytes(flowPackage, "invalidSetupData.zip"));

        checkExecutionIsActive(flowId, executionName);
    }

    @Test
    public void testGivenFlowExecutionWhenSuspendedThenFlowStatusSuspended() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        suspendExecution(flowExecution);

        checkExecutionSuspended(flowExecution);
    }

    @Test(expected = FlowAutomationException.class)
    public void testGivenFlowExecutionInExecutedStateWhenSuspendedThenThrownOperationNotAllowed() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupFile(flowExecution, "validSetupData.zip", getFlowdataFileBytes(flowPackage, "validSetupData.zip"));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        suspendExecution(flowExecution);
    }

    @Test
    public void testGivenFlowVersionWhenSuspendedThenAllExecutionsWereSuspended() {

        final FlowExecution flowExecution1 = startFlowExecution(flowDefinition, createUniqueInstanceName(flowId));
        final FlowExecution flowExecution2 = startFlowExecution(flowDefinition, createUniqueInstanceName(flowId));

        importFlow(flowId, getFlowPackageBytes("flow-with-setup-2.0.0"));

        suspendAllExecutionsForFlowVersion(flowId, initialVersion);

        checkExecutionSuspended(flowExecution1);
        checkExecutionSuspended(flowExecution2);
    }

    @Test(expected = FlowAutomationException.class)
    public void testGivenFlowVersionActiveWhenSuspendedThenThrownOperationNotAllowed() {
        startFlowExecution(flowDefinition, createUniqueInstanceName(flowId));
        startFlowExecution(flowDefinition, createUniqueInstanceName(flowId));

        suspendAllExecutionsForFlowVersion(flowId, initialVersion);
    }

    @Test
    public void testFlowExecutionEventsRecordedWhenFlowIsExecuted() {
        String executionName = createUniqueInstanceName(flowId);
        Date startDate = new Date();

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);
        completeUsertaskReviewAndConfirm(flowExecution);
        checkExecutionState(flowExecution, "COMPLETED");

        //Check Report Sheet
        FlowExecutionResource excelReport = downloadFlowExecutionReport(flowExecution);
        assertEquals(flowExecution.getName()+"-report.xlsx", excelReport.getName());

        Date endDate = new Date();

        final FlowExecutionEventFilter.Builder builder = new FlowExecutionEventFilter.Builder(flowId, executionName)
                .endDate(endDate)
                .startDate(startDate)
                .eventsSeverity(List.of(FlowExecutionEventSeverity.ERROR, FlowExecutionEventSeverity.INFO))
                .sortData(new SortData("severity", SortOrder.ASC));

        PaginatedData<FlowExecutionEvent> flowExecutionEvents = flowExecutionService.getExecutionEvents(builder.build());
        assertEquals(flowExecutionEvents.getNumberOfRecords(), 4);
    }

    @Test
    public void testExecutionDeletableInSetupPhase(){
        String executionName = createUniqueInstanceName(flowId);
        startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        deleteExecution(flowId,executionName);

        checkExecutionIsDeleted(flowId,executionName);
    }

    @After
    public void after() {
        removeFlow(flowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}
