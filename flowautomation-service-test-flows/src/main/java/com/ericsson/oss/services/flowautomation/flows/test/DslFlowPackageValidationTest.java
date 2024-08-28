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

import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.exception.ValidationException;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import org.junit.Test;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Flow package validation tests.
 */
public abstract class DslFlowPackageValidationTest extends FlowAutomationBaseTest {

    @Test
    public void testErrorCodeForInvalidDefaultDateTimeInUserTaskSchema() {

        String flowPackage = "validateDefaultDateTimeInJsonSchema";
        String flowId = "com.ericsson.oss.fa.flows.validateDefaultDateTimeInJsonSchema";

        try {
            importFlow(flowId, getFlowPackageBytes(flowPackage));
            fail("The flow package is invalid, a validation Exception should be thrown.");
        } catch (ValidationException e) {

            String dateTimeWidgetErrorMessage = "MESSAGE: setup/flow-input-schema.json is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ss.SSS'Z'].\nERROR REASON(S): \nInvalid Date Format for JSON Property: dateTimePicker/readOnlyDateTime";
            String nestedDateTimeInCheckBoxWidgetErrorMessage = "Invalid Date Format for JSON Property: nestedDateTime/checkboxNestingDateTime/nestedDateTimePicker";
            String nestedDateTimeInRadioBoxWidgetErrorMessage = "Invalid Date Format for JSON Property: nestedDateTime/nestingWithRadio/radioButtonNestingMultipleWidgets/nestedDateTimePicker";
            String nestedDateTimeInTableWidgetErrorMessage = "Invalid Date Format for JSON Property: dateTimeInTables/dateTimeInInformationalTable/dateTimeColumn";

            String errorMessage = dateTimeWidgetErrorMessage + "\n" + "\n" + nestedDateTimeInCheckBoxWidgetErrorMessage + "\n" + "\n" + nestedDateTimeInRadioBoxWidgetErrorMessage + "\n" + "\n" + nestedDateTimeInTableWidgetErrorMessage;

            assertEquals(FlowPackageErrorCode.FLOW_CANNOT_BE_IMPORTED.code(), e.getErrorReasons().get(0).getCode());
            assertEquals("Incorrect validation message returned.", errorMessage, e.getMessage().trim());

        }
    }

    @Test
    public void testErrorCodeForInvalidDefaultDateTimeInFlowReportJson() {

        String flowPackage = "validateDefaultDateTimeInReportJsonSchema";
        String flowId = "com.ericsson.oss.fa.flows.validateDefaultDateTimeInReportJsonSchema";

        try {
            importFlow(flowId, getFlowPackageBytes(flowPackage));
            fail("The flow package is invalid, a validation Exception should be thrown.");
        } catch (ValidationException e) {

            String errorMessage = "MESSAGE: report/flow-report-schema.json is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ss.SSS'Z'].\nERROR REASON(S): \nInvalid Date Format for JSON Property: simulationTable/simDateTime";

            assertEquals(FlowPackageErrorCode.FLOW_CANNOT_BE_IMPORTED.code(), e.getErrorReasons().get(0).getCode());
            assertEquals("Incorrect validation message returned.", errorMessage, e.getMessage().trim());

        }
    }
}
