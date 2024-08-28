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
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.ACTIVE_FLOW_VERSION_DOESNT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_PARSING_ERROR;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_VERSION_SYNTAX_INVALID;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class GetFlowInputSchemaErrorTest extends FlowAutomationNegativeTest {

    private static final String FLOW_ID = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow";

    private static final String FLOW_VERSION = "1.0.1";

    private static final String FLOW_PACKAGE = "import-base-flow-1.0.1";

    private static final String INVALID_FLOW_ID = "com.ericsson.oss.fa.flows.invalid.flow.name";

    private static final String INVALID_FLOW_VERSION = "1.0.34555555";

    private static final String PARSING_ERROR_FLOW_ID = "com.ericsson.oss.fa.flows.invalid.flow.input.schema";

    private static final String PARSING_ERROR_FLOW_PACKAGE = "invalid-flow-input-schema-1.0.0";

    private static final String WRONG_FLOW_VERSION = "1.0.34";

    @Before
    public void before() {
        importFlow(FLOW_ID, getFlowPackageBytes(FLOW_PACKAGE));
    }

    @Test
    public void test_getFlowInputSchema_FLOW_DOES_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowService.getFlowInputSchema(INVALID_FLOW_ID, INVALID_FLOW_VERSION),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_getFlowInputSchema_FLOW_VERSION_NOT_EXIST() {
        checkFlowFunctionFails(() ->
                        flowService.getFlowInputSchema(FLOW_ID, WRONG_FLOW_VERSION),
                FLOW_VERSION_NOT_EXIST);
    }

    @Test
    public void test_getFlowInputSchema_FLOW_VERSION_SYNTAX_INVALID() {
        checkFlowFunctionFails(() ->
                        flowService.getFlowInputSchema(FLOW_ID, INVALID_FLOW_VERSION),
                FLOW_VERSION_SYNTAX_INVALID);
    }

    @Test
    public void test_getFlowInputSchema_SCHEMA_NOT_FOUND() {
        checkFlowFunctionFails(() ->
                        flowService.getFlowInputSchema(FLOW_ID, FLOW_VERSION),
                SCHEMA_NOT_FOUND);
    }

    @Test
    public void test_getFlowInputSchema_ACTIVE_FLOW_VERSION_DOESNT_EXIST() {
        checkFlowFunctionFails(() -> {
            disableFlow(FLOW_ID);
            flowService.getFlowInputSchema(FLOW_ID, null);
        }, ACTIVE_FLOW_VERSION_DOESNT_EXIST);
    }

    @Test
    public void test_getFlowInputSchema_JSON_PARSING_ERROR() {
        checkFlowFunctionFails(() -> {
            importFlow(PARSING_ERROR_FLOW_ID, getFlowPackageBytes(PARSING_ERROR_FLOW_PACKAGE));
            flowService.getFlowInputSchema(PARSING_ERROR_FLOW_ID, null);
            removeFlow(PARSING_ERROR_FLOW_ID);
        }, JSON_PARSING_ERROR);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }
}
