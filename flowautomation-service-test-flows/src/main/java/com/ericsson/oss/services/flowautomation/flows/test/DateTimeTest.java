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
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.util.List;


import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.*;
import static org.junit.Assert.*;

public abstract class DateTimeTest extends FlowAutomationBaseTest {
    String flowPackage = "helloWorld-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.helloWorld";

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
    public void testValidRegexDateTimeFormatUniqueFlow() {
        FlowDefinition flowDefinitions = flowService.getFlowDefinition(flowId);
        String singleFlowCreatedDate = flowDefinitions.getFlowVersions().get(0).getCreatedDate();
        assertTrue(isValidDateTime(singleFlowCreatedDate));
   }



    @Test
    public void testValidRegexDateTimeFormatAllFlows() {
        final List<FlowDefinition> flowDefinitions = flowService.getFlows();
        String getAllFlowsCreatedDate = flowDefinitions.get(0).getFlowVersions().get(0).getCreatedDate();
        assertTrue(isValidDateTime(getAllFlowsCreatedDate));
    }

}

