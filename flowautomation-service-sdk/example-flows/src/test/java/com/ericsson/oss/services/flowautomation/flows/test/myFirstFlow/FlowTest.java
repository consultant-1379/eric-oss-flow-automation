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

package com.ericsson.oss.services.flowautomation.flows.test.myFirstFlow;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.*;

public class FlowTest extends FlowAutomationBaseTest {

    String flowPackage = "myFirstFlow";
    String flowId = "com.ericsson.oss.ae.fa.flows.my-first-flow";
    String message1 = "Hello";
    String message2 = "World";
    String defaultMessage = ": was the input";
    FlowDefinition flowDefinition;


    @Before
    public void before() {
        setOutputToConsole(true);
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @After
    public void after() {
        removeFlow(flowId);
    }

    @Test
    public void testFlowExecution_InteractiveSetup() {
        // Start Flow execution
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        // Wait for execution to become active
        checkExecutionIsActive(flowId, executionName);

        // Choose interactive setup
        completeUsertaskChooseSetupInteractive(flowExecution);

        // Complete first User Task once it becomes active
        completeUsertask(flowExecution, "Message 1", new UsertaskInputBuilder().
                input("Message 1 > Text", "Hello"));

        // Complete second User Task once it becomes active
        completeUsertask(flowExecution, "Message 2", new UsertaskInputBuilder().
                input("Message 2 > Text", "World"));

        completeUsertaskReviewAndConfirm(flowExecution);

        // Wait for execution to finish
        checkExecutionExecuted(flowExecution);

        // Expect empty summary report
        checkExecutionSummaryReport(flowExecution, "");

        // Check execution report has correct value
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "finalMessage", message1 + " " + message2 + " " + defaultMessage);
    }

    @Test
    public void testFlowExecution_InteractiveSetupWithBack() {
        // Start Flow execution
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        // Complete first User Task
        completeUsertask(flowExecution, "Message 1", new UsertaskInputBuilder().
                input("Message 1 > Text", "Hellou"));

        // Wait for User Task 2 to become active before going back
        checkUsertaskActive(flowExecution, "Message 2");

        // Go back to fix typo
        backUsertask(flowExecution);

        // Complete first User Task again
        completeUsertask(flowExecution, "Message 1", new UsertaskInputBuilder().
                input("Message 1 > Text", message1));

        // Complete second User Task and trigger validation error, Flow will not proceed from current user task
        completeUserTaskWithProcessingError(flowExecution, "Message 2", new UsertaskInputBuilder().
                input("Message 2 > Text", message1), "Messages cannot be the same or empty.", "error.fa.usertask.input.invalid");

        // Complete second User Task again
        completeUsertask(flowExecution, "Message 2", new UsertaskInputBuilder().
                input("Message 2 > Text", message2));

        completeUsertaskReviewAndConfirm(flowExecution);

        // Wait for execution to finish
        checkExecutionExecuted(flowExecution);

        // Expect empty summary report
        checkExecutionSummaryReport(flowExecution, "");

        // Check execution report has correct value
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "finalMessage", message1 + " " + message2 + " " + defaultMessage);
    }

    @Test
    public void testFlowExecution_FileSetup() {
        // Start Flow execution
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        // Complete setup phase with file input
        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup.zip"));

        completeUsertaskReviewAndConfirm(flowExecution);

        // Wait for execution to finish
        checkExecutionExecuted(flowExecution);

        // Expect empty summary report
        checkExecutionSummaryReport(flowExecution, "");

        // Check execution report has correct value
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "finalMessage", message1 + " " + message2 + " " + defaultMessage);
    }

    @Test
    public void testFlowExecution_FileSetup_InvalidFile() {
        // Start Flow execution
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        // Complete setup phase with invalid file input
        completeUsertaskChooseSetupFile(flowExecution, "setup-invalid.zip", getFlowdataFileBytes(flowPackage, "setup-invalid.zip"));

        // Check execution failed in setup phase
        checkExecutionState(flowExecution, "FAILED_SETUP");
    }

    @Test
    public void testFlowExecution_WithFile() {
        // Start Flow execution with input file
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecutionWithFile(flowDefinition, executionName, "flow-input.json", getFlowdataFileBytes(flowPackage, "flow-input.json"));

        // Wait for execution to finish
        checkExecutionExecuted(flowExecution);

        // Expect empty summary report
        checkExecutionSummaryReport(flowExecution, "");

        // Check execution report has correct value
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "finalMessage", message1 + " " + message2 + " " + defaultMessage);
    }
}
