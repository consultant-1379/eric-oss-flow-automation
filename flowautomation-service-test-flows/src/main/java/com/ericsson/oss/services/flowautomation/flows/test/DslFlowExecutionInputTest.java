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
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowInputCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

public abstract class DslFlowExecutionInputTest extends FlowAutomationBaseTest {

    String flowPackageWithoutSetup = "flow-without-setup-1.0.0";
    String flowIdWithoutSetup = "com.ericsson.oss.fa.flows.without-setup";

    String usertaskShowcaseFlowPackage = "usertaskShowcase-1.0.1";
    String usertaskShowcaseFlowId = "com.ericsson.oss.fa.flows.usertaskShowcase";


    FlowDefinition flowDefinitionWithoutSetup;
    FlowDefinition flowDefinitionUsertaskShowcase;

    int numInitialFlowsImported;

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        checkNumberOfExecutionsForFlow(flowIdWithoutSetup, 0);

        flowDefinitionWithoutSetup = importFlow(flowIdWithoutSetup, getFlowPackageBytes(flowPackageWithoutSetup));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);

        flowDefinitionUsertaskShowcase = importFlow(usertaskShowcaseFlowId, getFlowPackageBytes(usertaskShowcaseFlowPackage));
        checkNumberOfFlowsImported(2 + numInitialFlowsImported);
    }

    @Test
    public void testForFlowWithoutSetup() {
        final String executionName = createUniqueInstanceName(flowIdWithoutSetup);
        final FlowExecution flowExecution = startFlowExecution(flowDefinitionWithoutSetup, executionName);
        checkNoExecutionInputAvailable(flowExecution);
        checkExecutionExecuted(flowExecution);
    }

    @Test
    public void testForFlowInputSchemaAndDataInSettingUpPhase() {
        String executionName = createUniqueInstanceName(usertaskShowcaseFlowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinitionUsertaskShowcase, executionName);
        checkExecutionState(flowExecution, "SETTING_UP");
        checkNumberOfActiveExecutionsForFlow(usertaskShowcaseFlowId,1);
        String flowInputSchemaAndData = flowExecutionService.getFlowInputSchemaAndData(usertaskShowcaseFlowId, executionName);
        String expectedFlowInputSchemaAndData = "{\"$schema\":\"http://json-schema.org/draft-04/schema#\"," +
                "\"description\":\"Flow Instance Input Data supplied in Setup Phase\"," +
                "\"type\":\"object\",\"properties\":{}," +
                "\"additionalProperties\":false," +
                "\"required\":[\"usertaskSelection\"]," +
                "\"title\":\"Flow Instance Input Data\"," +
                "\"name\":\"Flow Instance Input Data\"," +
                "\"format\":\"informational\"}";

        assertEquals(flowInputSchemaAndData, expectedFlowInputSchemaAndData);
    }

    @Test
    public void testForFlowWithSetupWhenSetupPhaseInProgress() {
        final String executionName = createUniqueInstanceName(usertaskShowcaseFlowId);
        final FlowExecution flowExecution = startFlowExecution(flowDefinitionUsertaskShowcase, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Usertask Selection", buildUserTaskInput());

        checkExecutionInput(flowExecution, buildExpectedFlowInput());

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionInput(flowExecution, buildExpectedFlowInput());

        completeUsertask(flowExecution, "Get Execute Usertask Options", new UsertaskInputBuilder().
                input("Get Execute Usertask Options > Number Of Multi-instance Instances", 0));

        checkExecutionExecuted(flowExecution);
    }

    @Test
    public void testForFlowWithSetupWhenSetupPhaseFinished() {
        final String executionName = createUniqueInstanceName(usertaskShowcaseFlowId);
        final FlowExecution flowExecution = startFlowExecution(flowDefinitionUsertaskShowcase, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Usertask Selection", buildUserTaskInput());

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionInput(flowExecution, buildExpectedFlowInput());

        completeUsertask(flowExecution, "Get Execute Usertask Options", new UsertaskInputBuilder().
                input("Get Execute Usertask Options > Number Of Multi-instance Instances", 0));

        checkExecutionExecuted(flowExecution);
    }

    private UsertaskInputBuilder buildUserTaskInput() {
        return new UsertaskInputBuilder().
                input("Usertask Selection > Information", false).
                input("Usertask Selection > Text Inputs", false).
                input("Usertask Selection > Text Inputs With Information", false).
                input("Usertask Selection > Checkboxes", false).
                input("Usertask Selection > Radio Buttons", false).
                input("Usertask Selection > Selectboxes", false).
                input("Usertask Selection > Listboxes", false).
                input("Usertask Selection > Grouping And Nesting", false).
                input("Usertask Selection > Checkboxes With Nesting", false).
                input("Usertask Selection > Radio Buttons With Nesting", false);
    }

    private FlowInputCheckBuilder buildExpectedFlowInput() {
        return new FlowInputCheckBuilder().
                check("Usertask Selection > Information", false).
                check("Usertask Selection > Text Inputs", false).
                check("Usertask Selection > Text Inputs With Information", false).
                check("Usertask Selection > Checkboxes", false).
                check("Usertask Selection > Radio Buttons", false).
                check("Usertask Selection > Selectboxes", false).
                check("Usertask Selection > Listboxes", false).
                check("Usertask Selection > Grouping And Nesting", false).
                check("Usertask Selection > Checkboxes With Nesting", false).
                check("Usertask Selection > Radio Buttons With Nesting", false);
    }

    @After
    public void after() {
        removeFlow(flowIdWithoutSetup);
        removeFlow(usertaskShowcaseFlowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}
