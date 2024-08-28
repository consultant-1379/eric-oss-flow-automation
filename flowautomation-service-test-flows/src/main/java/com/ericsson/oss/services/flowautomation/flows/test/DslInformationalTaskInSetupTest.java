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
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

/**
 * The type Dsl informational task in setup test.
 */
public abstract class DslInformationalTaskInSetupTest extends FlowAutomationBaseTest {

    String flowId = "com.ericsson.oss.fa.flows.informational-user-task-in-setup";
    String flowPackage = "flow-with-informational-task-in-setup-1.0.0";
    FlowDefinition flowDefinition;

    int numInitialFlowsImported;

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfExecutionsForFlow(flowId, 0);
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @Test
    public void testFlowWithSetupNotificationTask() {
        final String executionName = createUniqueInstanceName(flowId);
        final FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertaskNoInput(flowExecution, "Notification");

        completeUsertaskReviewAndConfirm(flowExecution);

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @After
    public void after() {
        removeFlow(flowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}