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

import java.nio.charset.StandardCharsets;

/**
 * Test cases for Invalid flow packages using a simple DSL.
 */
public abstract class DslInvalidFlowPackagesImportTest extends FlowAutomationBaseTest {

    @Test
    public void testInvalidFlowsImport() {
        int numInitialFlowsImported = getNumberFlowsImported();

        String flowId = "com.ericsson.oss.fa.flow.with.missing.flowdefinition";
        String flowPackage = "invalid-flow-without-flowdefinition-1.0.0";
        checkFlowImportFails(flowId, getFlowPackageBytes(flowPackage), FlowPackageErrorCode.FLOW_DEFINITION_NOT_FOUND);

        flowId = "com.ericsson.oss.fa.flow.with.empty.package";
        flowPackage="invalid-empty-flow-package-1.0.0";
        checkFlowImportFails(flowId, getFlowPackageBytes(flowPackage), FlowPackageErrorCode.FLOW_PACKAGE_EMPTY);

        flowId = "com.ericsson.oss.fa.flow.without.execute.phase";
        flowPackage="invalid-flow-without-execute-phase-1.0.0";
        checkFlowImportFails(flowId, getFlowPackageBytes(flowPackage), FlowPackageErrorCode.EXECUTION_PHASE_MISSING_IN_FLOW_PACKAGE);

        flowId = "com.ericsson.oss.fa.flow.with.corrupted.package";
        //instead of byte array of zip, sending the string byte array which will be always corrupt
        checkFlowImportFails(flowId, "corruptedPackage".getBytes(StandardCharsets.UTF_8), FlowPackageErrorCode.FLOW_PACKAGE_EMPTY);

        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}
