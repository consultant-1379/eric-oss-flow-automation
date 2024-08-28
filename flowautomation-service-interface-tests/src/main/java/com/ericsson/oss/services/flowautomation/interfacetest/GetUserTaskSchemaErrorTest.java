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

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.TASK_NOT_FOUND;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;

import org.junit.Test;

public class GetUserTaskSchemaErrorTest extends FlowAutomationNegativeTest {

    @Test
    public void test_getUserTaskSchema_TASK_NOT_FOUND() {
        checkFlowFunctionFails(
                () -> flowExecutionService.getUserTaskSchema("noTaskId"),
                TASK_NOT_FOUND);
    }
}
