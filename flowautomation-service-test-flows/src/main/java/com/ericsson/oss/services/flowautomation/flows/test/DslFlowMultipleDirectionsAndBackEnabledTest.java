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
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

/**
 * Test cases for when a flow has multiple directions and the back feature is enabled.
 */
public abstract class DslFlowMultipleDirectionsAndBackEnabledTest extends FlowAutomationBaseTest {

    String flowPackage = "multipleDirectionsBackEnabled-1.0.0";
    String flowId = "com.ericsson.oss.fa.flows.multipleDirectionsBackEnabled";
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
    public void goBackAndForthToDifferentDirections() {

        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);

        checkUsertaskActive(flowExecution, "Select Phases");

        //Select -> Activation -> Preparation -> Final -> Preparation -> Activation -> Select
        completeUsertask(flowExecution, "Select Phases", new UsertaskInputBuilder()
                .input("Select Phases > Preparation Phase", true)
                .input("Select Phases > Activation Phase", true));

        completeUsertask(flowExecution, "Activation Phase", new UsertaskInputBuilder()
                .input("Activation Phase", ""));
        completeUsertask(flowExecution, "Preparation Phase", new UsertaskInputBuilder()
                .input("Preparation Phase", ""));

        backUsertask(flowExecution);
        checkUsertaskActive(flowExecution, "Preparation Phase");
        backUsertask(flowExecution);
        checkUsertaskActive(flowExecution, "Activation Phase");
        backUsertask(flowExecution);
        checkUsertaskActive(flowExecution, "Select Phases");

        //Select -> Preparation -> Select
        completeUsertask(flowExecution, "Select Phases", new UsertaskInputBuilder()
                .input("Select Phases > Preparation Phase", true));

        checkUsertaskActive(flowExecution, "Preparation Phase");
        backUsertask(flowExecution);
        checkUsertaskActive(flowExecution, "Select Phases");

        //Select -> Preparation -> Final -> Review and Confirm -> Execute
        completeUsertask(flowExecution, "Select Phases", new UsertaskInputBuilder()
                .input("Select Phases > Preparation Phase", true));
        completeUsertask(flowExecution, "Preparation Phase", new UsertaskInputBuilder()
                .input("Preparation Phase", ""));
        completeUsertask(flowExecution, "Final Task", new UsertaskInputBuilder()
                .input("Final Task", ""));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder()
                .check("Select Phases > Preparation Phase", true)
                .check("Select Phases > Activation Phase", false));

        completeUsertaskReviewAndConfirm(flowExecution);
        checkExecutionExecuted(flowExecution);
    }
}
