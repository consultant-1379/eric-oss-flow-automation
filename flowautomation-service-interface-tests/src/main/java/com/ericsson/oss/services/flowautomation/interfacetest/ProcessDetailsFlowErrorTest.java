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

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessDetailsFlowErrorTest extends FlowAutomationNegativeTest {

    String IMPORT_BASE_FLOW_1_0_1 = "helloWorld-1.0.1";
    String FLOW_ID = "com.ericsson.oss.fa.flows.helloWorld";


    @Before
    public void before() {
        importFlow(FLOW_ID, getFlowPackageBytes(IMPORT_BASE_FLOW_1_0_1));
    }

    @Test
    public void test_getFlowVersionProcessDetails_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowService.getProcessDetailsForFlowVersion("NoFlow", "1.0.1"),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_getFlowVersionProcessDetails_FLOW_VERSION_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowService.getProcessDetailsForFlowVersion("com.ericsson.oss.fa.flows.helloWorld", "0.0.0"),
                FLOW_VERSION_NOT_EXIST);
    }

    @Test
    public void test_getFlowVersionProcessDetails_FLOW_VERSION_SYNTAX_INVALID() {
        checkFlowFunctionFails(() ->
                        flowService.getProcessDetailsForFlowVersion("com.ericsson.oss.fa.flows.helloWorld","x.x.x"),
                FLOW_VERSION_SYNTAX_INVALID);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }


}
