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

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.realSleepMs;
import static org.hamcrest.Matchers.equalTo;

import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_HANDLING_INTERVAL;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.jayway.jsonassert.JsonAssert.with;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Multi-instance load control flow test cases
 */
public abstract class MultiInstanceLoadControlTest extends FlowAutomationBaseTest {

    String flowPackage = "multiInstance-loadControl-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.multiInstanceLoadControl";

    FlowDefinition flowDefinition;

    String incidentHandlingFlowPackage = "incident-handling";
    String incidentHandlingFlowId = "com.ericsson.oss.fa.internal.flows.incidentHandling";

    private static final String CONFIGURATION_GENERATE_INCIDENTS = "Configuration > Generate incidents";

    @Before
    public void before() {
        if (clientType == TestClientType.JSE) {
            System.setProperty(INCIDENT_HANDLING_INTERVAL, "5");
            final FlowDefinition incidentHandlingFlow = importFlow(incidentHandlingFlowId, getFlowPackageBytes(incidentHandlingFlowPackage));
            startFlowExecution(incidentHandlingFlow, "incident-handler");
        }

        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @After
    public void after() {
        removeFlow(flowId);

        if (clientType == TestClientType.JSE) {
            removeFlow(incidentHandlingFlowId);
        }
    }

    @Test
    @SuppressWarnings("squid:S1126")
    public void testEndToEndFlowExecution() {
        @SuppressWarnings({"squid:S00117"}) final int NUM_ELEMENTS = 20;

        final String executionName = createUniqueInstanceName(flowId);

        final FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        final UsertaskInputBuilder usertaskInputBuilder = new UsertaskInputBuilder();
        usertaskInputBuilder.input("Configuration > Number of Elements", "" + NUM_ELEMENTS);
        usertaskInputBuilder.input("Configuration > Load Control Pool Size", "4");
        usertaskInputBuilder.input("Configuration > Instance Sleep in Seconds", "10");
        if (clientType == TestClientType.JSE) {
            usertaskInputBuilder.input(CONFIGURATION_GENERATE_INCIDENTS, true);
        } else {
            usertaskInputBuilder.input(CONFIGURATION_GENERATE_INCIDENTS, false);
        }
        completeUsertask(flowExecution, "Configuration", usertaskInputBuilder);

        final UsertaskCheckBuilder usertaskCheckBuilder = new UsertaskCheckBuilder();
        usertaskCheckBuilder.check("Configuration > Number of Elements", "" + NUM_ELEMENTS);
        usertaskCheckBuilder.check("Configuration > Load Control Pool Size", "4");
        usertaskCheckBuilder.check("Configuration > Instance Sleep in Seconds", "10");
        if (clientType == TestClientType.JSE) {
            usertaskCheckBuilder.check(CONFIGURATION_GENERATE_INCIDENTS, true);
        } else {
            usertaskCheckBuilder.check(CONFIGURATION_GENERATE_INCIDENTS, false);
        }

        checkUsertaskReviewAndConfirm(flowExecution, usertaskCheckBuilder);

        completeUsertaskReviewAndConfirm(flowExecution);

        for (int i = 0; i < 6 * NUM_ELEMENTS; i++) {       // show report every 5s, should complete in reasonable time
            realSleepMs(5000);

            checkExecutionReport(flowExecution);
            if (isExecutionExecuted(flowExecution)) {
                break;
            }
        }

        checkExecutionExecuted(flowExecution);

        checkExecutionSummaryReport(flowExecution, "Our work is done here");

        checkExecutionIsInactive(flowId, executionName);

        // Check report
        final String report = getExecutionReport(flowExecution);
        with(report).assertThat("$.header.status", equalTo("COMPLETED"));
        with(report).assertThat("$.body.numElements", equalTo(NUM_ELEMENTS)).
                assertThat("$.body.numElementsOngoing", equalTo(0)).
                assertThat("$.body.numElementsQueued", equalTo(0)).
                assertThat("$.body.numElementsCompleted", equalTo(NUM_ELEMENTS));

        with(report).assertThat("$.body.elementsTable.length()", equalTo(NUM_ELEMENTS));
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            String expectedResult = "Success";
            if (clientType == TestClientType.JSE && i % 7 == 0) {     // for JSE - flow creates incident for every 7th element (starting at 0)
                expectedResult = "Error";
            } else if (i % 7 == 2) {    // flow creates failure for every 7th element (starting at 2)
                expectedResult = "Failure";
            }
            with(report).assertThat("$.body.elementsTable[" + i + "].result", equalTo(expectedResult));
        }
    }




}
