/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.rest.functest;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.realSleepMs;

import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RestHouseKeepingDelegateTesterTest extends FlowAutomationBaseTest {

    String houseKeepingFlowPackage = "house-keeping-delegate-tester";
    String FLOW_ID = "com.ericsson.oss.fa.internal.flows.houseKeepingDelegateTester";

    @Override
    protected TestClientType selectClientType() {
        return TestClientType.REST;
    }

    @Before
    public void before() {
        importFlow(FLOW_ID, getFlowPackageBytes(houseKeepingFlowPackage));
    }

    @Test
    public void testRestHouseKeepingDelegateTester() {
        String executionName = createUniqueInstanceName(FLOW_ID);
        flowService.executeFlow(FLOW_ID, executionName);

        //sleep to make sure the flow is executed
        realSleepMs(5000);

        List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(FLOW_ID).flowExecutionName(executionName).build());
        FlowExecution flowExecution = flowExecutions.get(0);

        checkExecutionSummaryReport(flowExecution, "Flow Executed Successfully");
    }


    @After
    public void after() {
        removeFlow(FLOW_ID);
    }

}

