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

import org.junit.Test;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;

public abstract class EnableFlowErrorTest extends FlowAutomationNegativeTest {

    private static final String NON_EXISTENT_FLOW_ID = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow-two";

    @Test
    public void test_enableFlow_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() -> flowService.enableFlow(NON_EXISTENT_FLOW_ID, true), FLOW_DOES_NOT_EXIST);

    }
}
