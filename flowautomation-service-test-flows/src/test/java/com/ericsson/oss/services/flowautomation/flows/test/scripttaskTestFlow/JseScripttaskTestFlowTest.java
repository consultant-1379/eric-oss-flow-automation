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

package com.ericsson.oss.services.flowautomation.flows.test.scripttaskTestFlow;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

public class JseScripttaskTestFlowTest extends FlowAutomationBaseTest {

    String flowPackage = "scripttaskTestFlow";
    String flowId = "com.ericsson.oss.fa.flows.scripttaskTestFlow";
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
    public void test() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);
        completeUsertask(flowExecution, "Flow Setup Data", new UsertaskInputBuilder().input("Flow Setup Data > Foo", "bar"));
        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().check("Flow Setup Data > Foo", "bar"));
        completeUsertaskReviewAndConfirm(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Reporting stuff");
        checkExecutionEventIsRecorded(flowExecution, "INFO", "Executing:" + flowId + "-" + executionName);
        checkExecutionExecuted(flowExecution);
    }

    @Override
    protected TestClientType selectClientType() {
        return TestClientType.JSE;
    }

}
