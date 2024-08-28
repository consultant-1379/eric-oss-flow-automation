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

package com.ericsson.oss.services.flowautomation.flows.test;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static org.junit.Assert.assertEquals;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DslActivateDeactivateFlowTest extends FlowAutomationBaseTest {
    String flowId = "com.ericsson.oss.fa.flows.with-setup";
    String flowPackage = "flow-with-setup-1.0.0";
    FlowDefinition flowDefinition;
    int noOfFlowsImported;

    @Before
    public void before() {
        noOfFlowsImported = getNumberFlowsImported();
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(noOfFlowsImported + 1);
    }

    @Test
    public void test_activateFlowVersion() {
        String flowPackageNew = "flow-with-setup-2.0.0";

        assertEquals(flowService.getFlowDefinition(flowId).getFlowVersions().size(), 1);
        checkFlowVersionIsActive(flowId, "1.0.0", true);

        importFlow(flowId, getFlowPackageBytes(flowPackageNew));
        assertEquals(flowService.getFlowDefinition(flowId).getFlowVersions().size(), 2);
        checkFlowVersionIsActive(flowId, "1.0.0", false);
        checkFlowVersionIsActive(flowId, "2.0.0", true);

        deactivateFlow(flowId, "1.0.0");
        checkFlowVersionIsActive(flowId, "1.0.0", false);
        checkFlowVersionIsActive(flowId, "2.0.0", true);

        activateFlow(flowId, "2.0.0");
        checkFlowVersionIsActive(flowId, "1.0.0", false);
        checkFlowVersionIsActive(flowId, "2.0.0", true);

        activateFlow(flowId, "1.0.0");
        checkFlowVersionIsActive(flowId, "1.0.0", true);
        checkFlowVersionIsActive(flowId, "2.0.0", false);

        deactivateFlow(flowId, "1.0.0");
        checkFlowVersionIsActive(flowId, "1.0.0", false);
        checkFlowVersionIsActive(flowId, "2.0.0", false);
    }

    @After
    public void after() {
        noOfFlowsImported = getNumberFlowsImported();
        removeFlow(flowId);
        checkNumberOfFlowsImported(noOfFlowsImported - 1);
    }
}
