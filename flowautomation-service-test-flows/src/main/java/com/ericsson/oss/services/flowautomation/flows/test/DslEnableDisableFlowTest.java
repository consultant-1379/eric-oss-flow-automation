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

import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

/**
 * Test cases for checking enabling and disabling of a flow.
 */
public abstract class DslEnableDisableFlowTest extends FlowAutomationBaseTest {
    String flowPackage = "helloWorld-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.helloWorld";

    @Test
    public void testEndToEndFlowExecution() {
        int numInitialFlowsImported = getNumberFlowsImported();

        FlowDefinition flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);

        checkFlowCanBeExecuted(flowDefinition);

        disableFlow(flowId);
        checkDisabledFlowCantBeExecuted(flowDefinition,createUniqueInstanceName(flowId));

        enableFlow(flowId);
        checkFlowCanBeExecuted(flowDefinition);

        enableFlow(flowId);
        checkFlowCanBeExecuted(flowDefinition);

        removeFlow(flowId);

        checkNumberOfFlowsImported(numInitialFlowsImported);
    }

    private void checkFlowCanBeExecuted(FlowDefinition flowDefinition) {
        final String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup.zip"));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionIsInactive(flowId, executionName);

        checkExecutionExecuted(flowExecution);
    }

}
