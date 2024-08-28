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

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_NOT_FOUND;

import org.junit.Test;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;

public abstract class GetFlowExecutionEventsErrorTest extends FlowAutomationNegativeTest {

    @Test
    public void test_getExecutionEvents_FLOW_EXECUTION_NOT_FOUND() {
        final FlowExecutionEventFilter.Builder builder = new FlowExecutionEventFilter.Builder("flowID", "executionName");
        checkFlowFunctionFails(() -> flowExecutionService.getExecutionEvents(builder.build()),
                FLOW_EXECUTION_NOT_FOUND);
    }
}
