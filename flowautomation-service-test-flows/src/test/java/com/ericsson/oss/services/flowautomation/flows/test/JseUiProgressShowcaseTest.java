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
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Test cases for UI progress showcase flow.
 */
public class JseUiProgressShowcaseTest extends FlowAutomationBaseTest {

    String flowPackage = "uiProgressShowcaseFlow-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.uiProgressShowcase";

    FlowDefinition flowDefinition;

    @Before
    public void before() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @After
    public void after() {
        removeFlow(flowId);
    }

    @Test
    public void testLoopingAndEventSubprocess_looping() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Looping and event subprocess", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of loops", new UsertaskInputBuilder().
                input("Get number of loops > Number of loops", 1));

        completeUsertask(flowExecution, "Ask what to throw", new UsertaskInputBuilder().
                input("Ask what to throw > Throw what ? > Nothing", true));

        completeUsertask(flowExecution, "Ask what to throw", new UsertaskInputBuilder().
                input("Ask what to throw > Throw what ? > Nothing", true));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "All is well");
    }

    @Test
    public void testLoopingAndEventSubprocess_loopingThrowError() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Looping and event subprocess", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of loops", new UsertaskInputBuilder().
                input("Get number of loops > Number of loops", 0));

        completeUsertask(flowExecution, "Ask what to throw", new UsertaskInputBuilder().
                input("Ask what to throw > Throw what ? > Error", true));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Error caught");
    }

    @Test
    public void testMultiInstanceCallActivity() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Multi-instance call activity", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Multi-instance call activity done");
    }

    @Test
    public void testMultiInstanceCallActivityMapName() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Multi-instance call activity map name", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Multi-instance call activity map name done");
    }

    @Test
    public void testMultiInstanceCallActivityMapNoName() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Multi-instance call activity map no name", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Multi-instance call activity map no name done");
    }

    @Test
    public void testMultiInstanceEmbeddedSubprocess() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Multi-instance embedded subprocess", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Multi-instance embedded subprocess done");
    }

    @Test
    public void testMultiInstanceEmbeddedSubprocessNoSleep() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Multi-instance embedded subprocess no sleep", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Multi-instance embedded subprocess no sleep done");
    }

    @Test
    public void testCompensation() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Compensation", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Undoing something");
    }

    @Override
    protected TestClientType selectClientType() {
        return TestClientType.JSE;
    }

}
