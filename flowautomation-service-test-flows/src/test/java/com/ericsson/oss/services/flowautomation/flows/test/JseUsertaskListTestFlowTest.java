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
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Test cases for Usertask List Test Flow
 */
public class JseUsertaskListTestFlowTest extends FlowAutomationBaseTest {

    String flowPackage = "usertaskListTestFlow";
    String flowId = "com.ericsson.oss.fa.flows.usertaskListTestFlow";
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
    public void testEndToEndFlowWithoutSetup() {
        final String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertaskNoInput(flowExecution, "License Check");

        completeUsertask(flowExecution, "Get Settings", new UsertaskInputBuilder().
                input("Get Settings > Single Usertasks > Initial Delay (seconds)", 5).
                input("Get Settings > Single Usertasks > Number Of Usertasks", 2).
                input("Get Settings > Same Name Single Usertasks > Number Of Usertasks", 2).
                input("Get Settings > Single NameExtra Usertasks > Number Of Usertasks", 2).
                input("Get Settings > Multi Instance Usertasks > Number Of Multi Instance Usertasks", 2).
                input("Get Settings > Multi Instance Usertasks > Number Of Usertasks Per Multi", 2).
                input("Get Settings > Multi Instance NameExtra Usertasks > Number Of Multi Instance Usertasks", 2).
                input("Get Settings > Multi Instance NameExtra Usertasks > Number Of Usertasks Per Multi", 2));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertaskNoInput(flowExecution, "Single usertask 1");
        completeUsertaskNoInput(flowExecution, "Same name single usertask 1");
        completeUsertaskNoInput(flowExecution, "Same name single usertask 1");
        completeUsertaskNoInput(flowExecution, "Single nameExtra usertask 1 - Extra");

        completeUsertaskNoInput(flowExecution, "Multi usertask 1");
        completeUsertaskNoInput(flowExecution, "Multi usertask 1");

        completeUsertaskNoInput(flowExecution, "Multi nameExtra usertask 1 - Extra 1");
        completeUsertaskNoInput(flowExecution, "Multi nameExtra usertask 1 - Extra 2");

        completeUsertaskNoInput(flowExecution, "Single usertask 2");
        completeUsertaskNoInput(flowExecution, "Same name single usertask 2");
        completeUsertaskNoInput(flowExecution, "Same name single usertask 2");
        completeUsertaskNoInput(flowExecution, "Single nameExtra usertask 2 - Extra");

        completeUsertaskNoInput(flowExecution, "Multi usertask 2");
        completeUsertaskNoInput(flowExecution, "Multi usertask 2");

        completeUsertaskNoInput(flowExecution, "Multi nameExtra usertask 2 - Extra 1");
        completeUsertaskNoInput(flowExecution, "Multi nameExtra usertask 2 - Extra 2");

        checkExecutionExecuted(flowExecution);
    }
}
