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

import org.junit.Test;

import com.ericsson.oss.services.flowautomation.error.FlowErrorCode;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

/**
 * Test cases for Hello World flow using a simple DSL.
 */
public abstract class DslHelloWorldChangeFlowNameTest extends FlowAutomationBaseTest {

    static final String FLOW_ID = "com.ericsson.oss.fa.flows.helloWorld";

    @Test
    public void tesImportSameVersionDifferentName() {
        final String flowPackageName = "helloWorld-1.0.1";

        final int numInitialFlowsImported = getNumberFlowsImported();

        importFlow(FLOW_ID, getFlowPackageBytes(flowPackageName));

        checkNumberOfFlowsImported(1 + numInitialFlowsImported);

        final String flowDiffNameSameVersion = "helloWorld-changed-flowname-same-version-1.0.1";
        checkFlowImportFails(FLOW_ID, getFlowPackageBytes(flowDiffNameSameVersion), FlowErrorCode.FLOW_ALREADY_EXISTS);
        removeFlow(FLOW_ID);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }

    @Test
    public void tesImportDiffVersionDifferentName() {
        final String flowPackageName = "helloWorld-1.0.1";

        final int numInitialFlowsImported = getNumberFlowsImported();

        FlowDefinition flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(flowPackageName));
        assertEquals("Hello World", flowDefinition.getName());

        checkNumberOfFlowsImported(1 + numInitialFlowsImported);

        final String flowDiffNameDiffVersion = "helloWorld-changed-flowname-new-version-1.0.2";
        flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(flowDiffNameDiffVersion));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
        //Checking flow name is actually the new one
        assertEquals("Hello World Changed Name", flowDefinition.getName());

        removeFlow(FLOW_ID);
    }
}
