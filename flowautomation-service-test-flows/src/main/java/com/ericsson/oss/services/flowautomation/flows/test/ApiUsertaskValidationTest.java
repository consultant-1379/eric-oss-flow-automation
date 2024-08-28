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

import static java.lang.String.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

public abstract class ApiUsertaskValidationTest extends FlowAutomationBaseTest {

    private String userTaskInputJsonFormat = "{\"inputUserTask\":{\"inputValue\":\"%s\"}}";
    private String flowPackage = "flow-usertask-input-validation-1.0.1";
    private String flowId = "com.ericsson.oss.fa.flows.usertaskvalidation";

    private FlowDefinition flowDefinition;

    @Before
    public void before() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @Test
    public void testGivenFlowWithUsertaskValidationWhenExecutedThenThrowsError() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        checkExecutionIsActive(flowId, executionName);

        UserTask userTask = checkUsertaskActive(flowExecution, "Take user input");

        assertThatCompleteUsertaskFailsWithAnErrorCode(userTask, "invalidIntegerValue", "error.fa.usertask.input.invalid");
        assertThatCompleteUsertaskFailsWithAnErrorCode(userTask, "1000", "error.fa.usertask.input.invalid");
        flowExecutionService.completeUserTask(userTask.getId(), new HashMap<>(), format(userTaskInputJsonFormat, "110"));

        checkExecutionExecuted(flowExecution);
    }

    private void assertThatCompleteUsertaskFailsWithAnErrorCode(UserTask userTasks, String usertaskInputValue, String expectedErrorCode) {
        try {
            flowExecutionService.completeUserTask(userTasks.getId(), new HashMap<>(), format(userTaskInputJsonFormat, usertaskInputValue));
            fail("The complete usertask with invalid input should throw an exception.");
        } catch (Exception exception) {
            Throwable rootCause = getRootCause(exception);
            if (FlowAutomationException.class.isAssignableFrom(rootCause.getClass())) {
                FlowAutomationException error = (FlowAutomationException) rootCause;
                assertEquals(expectedErrorCode, error.getErrorReasons().get(0).getCode());
            } else {
                fail("Unexpected exception happened: " + exception.getMessage());
            }
        }
    }

    @After
    public void after() {
        removeFlow(flowId);
    }
}
