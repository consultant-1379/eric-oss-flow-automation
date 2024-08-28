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
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_PARSING_ERROR;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class GetExecutionReportSchemaErrorTest extends FlowAutomationNegativeTest {

    private static final String FLOW_ID = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow";

    private static final String FLOW_PACKAGE = "import-base-flow-1.0.0";

    private static final String INVALID_FLOW_ID = "com.ericsson.oss.fa.internal.flows.invalid.flow.id";

    private static final String FLOW_EXECUTION_NAME = "executionName";

    private static final String REPORT_SCHEMA_MISSING_FLOW_ID = "com.ericsson.oss.fa.flows.invalid.flow.report.schema";

    private FlowDefinition flowDefinition;

    private FlowDefinition missingIdFlowDefinition;

    @Before
    public void before() {
        flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(FLOW_PACKAGE));
        missingIdFlowDefinition = importFlow(REPORT_SCHEMA_MISSING_FLOW_ID, getFlowPackageBytes("invalid-flow-report-schema-1.0.0"));
    }

    @Test
    public void test_getExecutionReportSchema_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.getExecutionReportSchema(INVALID_FLOW_ID, FLOW_EXECUTION_NAME),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_getExecutionReportSchema_FLOW_EXECUTION_NOT_FOUND() {
        checkFlowFunctionFails(() ->
                        flowExecutionService.getExecutionReportSchema(FLOW_ID, FLOW_EXECUTION_NAME),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @Test
    public void test_getExecutionReportSchema_SCHEMA_NOT_FOUND() {
        final String executionName = createUniqueInstanceName(FLOW_ID);
        final FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        checkFlowFunctionFails(() -> {
            flowExecutionService.getExecutionReportSchema(flowExecution.getFlowId(), executionName);
            }, SCHEMA_NOT_FOUND);
    }

    @Test
    public void test_getExecutionReportSchema_JSON_PARSING_ERROR() {
        final String executionName = createUniqueInstanceName(REPORT_SCHEMA_MISSING_FLOW_ID);
        final FlowExecution flowExecution = startFlowExecution(missingIdFlowDefinition, executionName);
        checkFlowFunctionFails(() -> {
            flowExecutionService.getExecutionReportSchema(flowExecution.getFlowId(), executionName);
            }, JSON_PARSING_ERROR);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
        removeFlow(REPORT_SCHEMA_MISSING_FLOW_ID);
    }

}
