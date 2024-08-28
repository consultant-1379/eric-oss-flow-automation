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

package com.ericsson.oss.services.flowautomation.interfacetest;

import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_DOES_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_VERSION_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_VERSION_SYNTAX_INVALID;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;

public abstract class ActivateFlowTest extends FlowAutomationNegativeTest {
    private static final String FLOW_ID = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow";
    private static final String IMPORT_BASE_FLOW_1_0_1 = "import-base-flow-1.0.1";

    @Before
    public void before() {
        importFlow(FLOW_ID, getFlowPackageBytes(IMPORT_BASE_FLOW_1_0_1));
    }

    @Test
    public void test_activateFlowVersion_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowService.activateFlowVersion("NoFlow", "flowVersion", true),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_activateFlowVersion_FLOW_VERSION_SYNTAX_INVALID() {
        checkFlowFunctionFails(() ->
                        flowService.activateFlowVersion(FLOW_ID, "123.123.123xx", true),
                FLOW_VERSION_SYNTAX_INVALID);
    }

    @Test
    public void test_activateFlowVersion_FLOW_VERSION_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowService.activateFlowVersion(FLOW_ID, "1.0.12", true),
                FLOW_VERSION_NOT_EXIST);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }

}