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

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

/**
 * Test cases for Flow that uses FlowExecutionContext API to retrieve the flowId and flowExecutionName.
 */
public abstract class DslFlowExecutionFlowApiTest extends FlowAutomationBaseTest {

    String flowPackage = "flow-using-flowexecution-api-1.0.0";
    String flowId = "com.ericsson.oss.fa.flows.using.flowapi.flowexecution";
    FlowDefinition flowDefinition;

    int numInitialFlowsImported;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @Test
    public void testFlowUsingFlowExecutionApi() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionIsInactive(flowId, executionName);

        checkExecutionSummaryReport(flowExecution, String.format("completed:%s-%s", flowId, executionName));
    }


    @After
    public void after() {
        removeFlow(flowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}
