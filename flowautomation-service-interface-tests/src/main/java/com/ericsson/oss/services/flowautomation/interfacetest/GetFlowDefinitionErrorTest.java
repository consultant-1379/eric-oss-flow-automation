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

public abstract class GetFlowDefinitionErrorTest extends FlowAutomationNegativeTest {

    private static final String INVALID_FLOW_ID = "com.ericsson.oss.fa.flows.invalid.flow.name";

    @Test
    public void test_getFlow_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowService.getFlowDefinition(INVALID_FLOW_ID),
                FLOW_DOES_NOT_EXIST);
    }
}
