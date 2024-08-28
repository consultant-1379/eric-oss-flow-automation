/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

public class DslSdkActivityDiagramProgressShowcaseFlowTest extends FlowAutomationBaseTest {
    String flowPackage = "sdkActivityDiagramProgressShowcaseFlow-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.sdkActivityDiagramProgressShowcaseFlow";

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
    public void testParallelMultiInstanceCallActivity() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Parallel Multi-instance call activity", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Parallel multi-instance call activity done");
    }

    @Test
    public void testParallelMultiInstanceCallActivityMapName() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Parallel Multi-instance call activity map name", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Parallel Multi-instance call activity map name done");
    }

    @Test
    public void testParallelMultiInstanceCallActivityMapNoName() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Parallel Multi-instance call activity map no name", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Parallel multi-instance call activity map no name done");
    }

    @Test
    public void testParallelMultiInstanceEmbeddedSubprocess() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Parallel Multi-instance embedded subprocess", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Parallel multi-instance embedded subprocess done");
    }

    @Test
    public void testParallelMultiInstanceEmbeddedSubprocessNoSleep() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Parallel Multi-instance embedded subprocess no sleep", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 5));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Parallel multi-instance embedded subprocess no sleep done");
    }

    @Test
    public void testSequentialMultiInstanceCallActivity() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Sequential Multi-instance call activity", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 3));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Sequential Multi-instance call activity done");
    }

    @Test
    public void testSequentialMultiInstanceCallActivityMapName() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Sequential Multi-instance call activity map name", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 3));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Sequential Multi-instance call activity map name done");
    }

    @Test
    public void testSequentialMultiInstanceCallActivityMapNoName() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Sequential Multi-instance call activity map no name", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 3));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Sequential multi-instance call activity map no name done");
    }

    @Test
    public void testSequentialMultiInstanceEmbeddedSubprocess() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Sequential Multi-instance embedded subprocess", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 3));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Sequential multi-instance embedded subprocess done");
    }

    @Test
    public void testSequentialMultiInstanceEmbeddedSubprocessNoSleep() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Scenario Selection", new UsertaskInputBuilder().
                input("Scenario Selection > Sequential Multi-instance embedded subprocess no sleep", true));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, "Get number of instances", new UsertaskInputBuilder().
                input("Get number of instances > Number of instances", 3));

        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Sequential multi-instance embedded subprocess no sleep done");
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
}
