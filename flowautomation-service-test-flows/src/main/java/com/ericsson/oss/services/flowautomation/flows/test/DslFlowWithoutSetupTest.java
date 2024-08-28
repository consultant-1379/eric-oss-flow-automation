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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

/**
 * Test cases for Flow without setup.
 * <p>
 * Note that while this test uses the FAS JSE API, it can also work against the FAS REST API by simply supplying a REST client based implementation of
 * FAS API.
 */
public abstract class DslFlowWithoutSetupTest extends FlowAutomationBaseTest {

    String flowPackage = "flow-without-setup-1.0.0";
    String flowId = "com.ericsson.oss.fa.flows.without-setup";
    FlowDefinition flowDefinition;

    int numInitialFlowsImported;

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @Test
    public void testEndToEndFlowWithoutSetup() {
        final String executionName = createUniqueInstanceName(flowId);
        startFlowExecution(flowDefinition, executionName);
        checkExecutionIsInactive(flowId, executionName);
    }

    @After
    public void after() {
        removeFlow(flowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}
