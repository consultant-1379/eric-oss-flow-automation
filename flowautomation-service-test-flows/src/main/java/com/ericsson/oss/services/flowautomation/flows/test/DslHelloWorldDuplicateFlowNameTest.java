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

import org.junit.Test;

import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

/**
 * Test cases for Hello World flow using a simple DSL.
 */
public abstract class DslHelloWorldDuplicateFlowNameTest extends FlowAutomationBaseTest {

    @Test
    public void testEndToEndFlowExecution() {
        String flowId = "com.ericsson.oss.fa.flows.helloWorld";
        final String flowPackageName = "helloWorld-1.0.1";

        int numInitialFlowsImported = getNumberFlowsImported();

        importFlow(flowId, getFlowPackageBytes(flowPackageName));

        checkNumberOfFlowsImported(1 + numInitialFlowsImported);

        final String flowIdOfFlowWithDuplicateFlowName = "com.ericsson.oss.fa.flows.helloWorld.duplicate.flowname";
        final String flowPackageNameWithDuplicateFlowName = "helloWorld-duplicate-flowname-1.0.1";
        checkFlowImportFails(flowIdOfFlowWithDuplicateFlowName, getFlowPackageBytes(flowPackageNameWithDuplicateFlowName), FlowPackageErrorCode.FLOW_NAME_ALREADY_INUSE);

        removeFlow(flowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}
