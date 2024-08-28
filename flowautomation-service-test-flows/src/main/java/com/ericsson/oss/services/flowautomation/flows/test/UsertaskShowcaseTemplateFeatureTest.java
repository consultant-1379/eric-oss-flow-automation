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
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * The type Usertask showcase template feature test.
 */
@SuppressWarnings({"squid:S00100"})
public abstract class UsertaskShowcaseTemplateFeatureTest extends FlowAutomationBaseTest {

    public static final String DYNAMIC_MULTI_TASK_USER_TASK = "Dynamic Multi-Task UserTask";
    /**
     * The Flow package.
     */
    String flowPackage = "usertaskShowcaseTemplateFeature-1.0.1";
    /**
     * The Flow id.
     */
    String flowId = "com.ericsson.oss.fa.flows.usertaskShowcaseTemplateFeature";
    /**
     * The Flow definition.
     */
    FlowDefinition flowDefinition;

    /**
     * Before.
     */
    @Before
    public void before() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    /**
     * After.
     */
    @After
    public void after() {
        removeFlow(flowId);
    }


    /**
     * Test end to end flow execution file input two execute usertasks.
     */
    @Test
    public void testEndToEndFlowExecution_FileInput_ExecutionUserTasks() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup.zip"));

        checkUsertaskReviewAndConfirm(flowExecution,new UsertaskCheckBuilder().
        check("Dynamic Object 1 > Task 1 > Task 1", true).
        check("Dynamic Object 1 > Task 2 > Task 2", false).
        check("Dynamic Object 1 > List Box 1", "ITEM3").
        check("Dynamic Object 2 > Task 1 > Task 1", true).
        check("Dynamic Object 2 > Task 2 > Task 2", true).
        check("Dynamic Object 2 > Task 3 > Task 3", false).
        check("Dynamic Object 2 > Task 4 > Task 4", false).
        check("Dynamic Object 2 > List Box 2", "ITEM6"));

        completeUsertaskReviewAndConfirm(flowExecution);


        completeUsertask(flowExecution, DYNAMIC_MULTI_TASK_USER_TASK, new UsertaskInputBuilder().
        input("Dynamic Multi-Task Usertask > NodeType 2G > Pre Install Health Check > Pre Install Health Check CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 2G > Pre Install License Check > Pre Install License Check CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 3G > Pre Install Health Check > Pre Install Health Check CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 3G > Pre Install License Check > Pre Install License Check CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 3G > Pre Install Backup > Pre Install Backup CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 3G > Post Install Backup > Post Install Backup CheckBox", true));

        completeUsertask(flowExecution, "Get Execute Usertask Options", new UsertaskInputBuilder().
        input("Get Execute Usertask Options > Number Of Multi-instance Instances", 2));

        completeUsertaskNoInput(flowExecution, "Multi-instance Usertask - Instance0");
        completeUsertaskNoInput(flowExecution, "Multi-instance Usertask - Instance1");

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Interactive_SetupPhase_ExecutionUserTasks() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "Select Dynamic Objects", new UsertaskInputBuilder().
        input("Select Dynamic Objects > Dynamic Object 1", true).
        input("Select Dynamic Objects > Dynamic Object 2", true));

        completeUsertask(flowExecution, DYNAMIC_MULTI_TASK_USER_TASK, new UsertaskInputBuilder().
        input("Dynamic Multi-Task Usertask > Dynamic Object 1 > Task 1 > Task 1", true).
        input("Dynamic Multi-Task Usertask > Dynamic Object 1 > Task 2 > Task 2", true).
        input("Dynamic Multi-Task Usertask > Dynamic Object 1 > List Box 1", "Item 2").
        input("Dynamic Multi-Task Usertask > Dynamic Object 2 > Task 1 > Task 1", true).
        input("Dynamic Multi-Task Usertask > Dynamic Object 2 > Task 2 > Task 2", true).
        input("Dynamic Multi-Task Usertask > Dynamic Object 2 > Task 3 > Task 3", true).
        input("Dynamic Multi-Task Usertask > Dynamic Object 2 > Task 4 > Task 4", true).
        input("Dynamic Multi-Task Usertask > Dynamic Object 2 > List Box 2", "Item 5"));

        checkUsertaskReviewAndConfirm(flowExecution,new UsertaskCheckBuilder().
        check("Dynamic Object 1 > Task 1 > Task 1", true).
        check("Dynamic Object 1 > Task 2 > Task 2", true).
        check("Dynamic Object 1 > List Box 1", "ITEM2").
        check("Dynamic Object 2 > Task 1 > Task 1", true).
        check("Dynamic Object 2 > Task 2 > Task 2", true).
        check("Dynamic Object 2 > Task 3 > Task 3", true).
        check("Dynamic Object 2 > Task 4 > Task 4", true).
        check("Dynamic Object 2 > List Box 2", "ITEM5"));


        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, DYNAMIC_MULTI_TASK_USER_TASK, new UsertaskInputBuilder().
        input("Dynamic Multi-Task Usertask > NodeType 2G > Pre Install Health Check > Pre Install Health Check CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 2G > Pre Install License Check > Pre Install License Check CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 3G > Pre Install Health Check > Pre Install Health Check CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 3G > Pre Install License Check > Pre Install License Check CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 3G > Pre Install Backup > Pre Install Backup CheckBox", true).
        input("Dynamic Multi-Task Usertask > NodeType 3G > Post Install Backup > Post Install Backup CheckBox", true));


        completeUsertask(flowExecution, "Get Execute Usertask Options", new UsertaskInputBuilder().
        input("Get Execute Usertask Options > Number Of Multi-instance Instances", 2));

        completeUsertaskNoInput(flowExecution, "Multi-instance Usertask - Instance0");
        completeUsertaskNoInput(flowExecution, "Multi-instance Usertask - Instance1");

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

}
