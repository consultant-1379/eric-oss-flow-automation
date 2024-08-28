/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

/**
 * Validate User Task Schema tests
 */
public abstract class DslValidateUserTaskSchemaTest extends FlowAutomationBaseTest {

    private static final String FLOW_PACKAGE = "validateUserTaskSchema";
    private static final String FLOW_ID = "com.ericsson.oss.fa.flows.validateUserTaskSchema";
    private FlowExecution flowExecution;
    private static final String CORRECT_DATE_TIME_INPUT = "2022-04-14T16:30:25.000Z";

    @Before
    public void before() {
        FlowDefinition flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(FLOW_PACKAGE));
        String executionName = createUniqueInstanceName(FLOW_ID);
        flowExecution = startFlowExecution(flowDefinition, executionName);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }

    @Test
    public void logWarnMessagesForInvalidDateTimeFormat() {
        checkNumberOfActiveExecutionsForFlow(FLOW_ID, 1);
        completeUsertaskChooseSetupInteractive(flowExecution);
        completeDateTimeUserTask();
        completeNestedDateTimeUserTask();
        completeDateTimeInTablesUserTask();
        completeUsertaskReviewAndConfirm(flowExecution);
        completeUserTaskExecutePhase();
        checkExecutionExecuted(flowExecution);
    }

    private void completeDateTimeUserTask() {
        final String userTaskName = "Date and Time Picker";
        final String userTaskNameExtra = userTaskName + " - Validate Schema";

        completeUsertask(flowExecution, userTaskNameExtra, new UsertaskInputBuilder()
            .input(String.format("%s > Date-Time Picker", userTaskNameExtra), CORRECT_DATE_TIME_INPUT)
            .input(String.format("%s > Read-Only Date Time Picker", userTaskNameExtra), CORRECT_DATE_TIME_INPUT));

        checkDateTimeEventMessage("2022-09-27 12:39:00", userTaskName);
        checkDateTimeEventMessage("2022-09-27 11:39:00", userTaskName);
    }

    private void completeNestedDateTimeUserTask() {
        final String userTaskName = "Nested Date-Time";
        final String radioButtonInputName = "Nested Date-Time > Select option > Radio Button nesting a Popup Date-Time Picker > Date-Time nested to a Radio Button";
        final String checkboxInputName = "Nested Date-Time > Checkbox nesting a Popup Date-Time Picker > Date-Time nested to a Checkbox";
        completeUsertask(flowExecution, "Nested Date-Time", new UsertaskInputBuilder()
                .input(checkboxInputName, CORRECT_DATE_TIME_INPUT)
                .input(radioButtonInputName, CORRECT_DATE_TIME_INPUT));

        checkDateTimeEventMessage("2022-08-10T05:30:00+0200", userTaskName);
        checkDateTimeEventMessage("2022-08-10T05:30:00+0100", userTaskName);
    }

    private void completeDateTimeInTablesUserTask() {
        final String userTaskName = "Date-Time in Tables";
        completeUsertask(flowExecution, userTaskName, new UsertaskInputBuilder()
                .input("Date-Time in Tables > Editable Table with Date-Time", new ArrayList<>()));

        //Selection Table
        checkDateTimeEventMessage("2022-01-05T01:30:00-01:00", userTaskName);
        checkDateTimeEventMessage("2022-01-05T02:30:00-01:00", userTaskName);
        checkDateTimeEventMessage("2022-01-05T03:30:00-01:00", userTaskName);

        //Informational Table
        checkDateTimeEventMessage("2022-02-05T01:30:00-01:00", userTaskName);
        checkDateTimeEventMessage("2022-02-05T02:30:00-01:00", userTaskName);
        checkDateTimeEventMessage("2022-02-05T03:30:00-01:00", userTaskName);

        //Read-Only Table
        checkDateTimeEventMessage("2022-03-05T01:30:00-01:00", userTaskName);
        checkDateTimeEventMessage("2022-03-05T02:30:00-01:00", userTaskName);
        checkDateTimeEventMessage("2022-03-05T03:30:00-01:00", userTaskName);

        //Edit Table
        checkDateTimeEventMessage("2022-06-01", userTaskName);
        checkDateTimeEventMessage("2022-06-02", userTaskName);
        checkDateTimeEventMessage("2022-06-03", userTaskName);
        checkDateTimeEventMessage("2022-06-04", userTaskName);
    }

    private void completeUserTaskExecutePhase() {
        final String userTaskName = "Date-Time in the Execute Phase";
        final String userTaskNameExtra = userTaskName + " - Test Validate Schema Execute Phase";
        completeUsertask(flowExecution, userTaskNameExtra, new UsertaskInputBuilder()
                .input(String.format("%s > Date-Time Picker", userTaskNameExtra), CORRECT_DATE_TIME_INPUT));

        checkDateTimeEventMessage("2022-10-10T12:30:00", userTaskName);
    }

    private void checkDateTimeEventMessage(String dateTimeValue, String userTaskName) {
        String message = String.format("String %s is invalid against accepted date format(s) [yyyy-MM-dd'T'HH:mm:ss.SSS'Z']",
                dateTimeValue);
        String target = "User Task Schema: " + userTaskName;

        checkExecutionEventIsRecorded(flowExecution, FlowExecutionEventSeverity.WARNING, message, target);
    }
}
