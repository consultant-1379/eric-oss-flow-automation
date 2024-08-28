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
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * File handling flow test cases
 */
public abstract class FileHandlingTest extends FlowAutomationBaseTest {

    String flowPackage = "fileHandling-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.fileHandling";

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
    public void testEndToEndFlowExecution() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertask(flowExecution, "Upload File", new UsertaskInputBuilder().
                input("Upload File > File", "file.txt", getFlowdataFileBytes(flowPackage, "file.txt")));

        completeUsertaskNoInput(flowExecution, "Wait");

        checkExecutionSummaryReport(flowExecution, "Our work is done here");

        checkExecutionReport(flowExecution);

        checkExecutionIsInactive(flowId, executionName);
    }

}
