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
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_CANNOT_BE_DELETED;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import org.junit.Test;

public abstract class DeleteFlowErrorTest extends FlowAutomationNegativeTest {

    @Test
    public void test_deleteFlow_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() -> flowService.deleteFlow("NoFlow", true), FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_deleteFlow_FLOW_CANNOT_BE_DELETED() {
        checkFlowFunctionFails(() ->
                        flowService.deleteFlow("com.ericsson.oss.fa.internal.flows.houseKeeping", false),
                FLOW_CANNOT_BE_DELETED);
    }
}
