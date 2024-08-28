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

import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_ALREADY_EXISTS;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_PACKAGE_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_PARSE_ERROR;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_VERSION_SYNTAX_INVALID;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_VERSION_NOT_ALLOWED;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_INVALID;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_NAME_ALREADY_INUSE;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;
import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class ImportFlowErrorTest extends FlowAutomationNegativeTest {

    private static final String FLOW_ID = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow";

    private static final String FLOW_ID_INVALID_FLOW_DEFINITION_FILE = "com.ericsson.oss.fa.flows.interfacetest.testFlowDefinitionInvalid";

    private static final String FLOW_ID_INVALID_FLOW_DEFINITION_PARSE_ERROR = "com.ericsson.oss.fa.flows.interfacetest.testFlowDefinitionParseError";

    private static final String FLOW_ID_INVALID_FLOW_DEFINITION_SAME_NAME_ERROR = "com.ericsson.oss.fa.flows.interfacetest.testFlowNameAlreadyInUse";

    private static final String FLOW_ID_INVALID_FLOW_VERSION = "com.ericsson.oss.fa.flows.interfacetest.testInvalidFlowSyntax";

    private static final String IMPORT_BASE_FLOW_1_0_1 = "import-base-flow-1.0.1";

    private static final String INVALID_FLOW_PACKAGE_WITH_LOWER_VERSION = "import-base-flow-1.0.0";

    private static final String INVALID_FLOW_WITHOUT_DEFINITION = "invalid-flow-without-flowdefinition-1.0.0";

    private static final String INVALID_FLOW_DEFINITION_PARSE_ERROR = "invalid-flow-definition-parse-error-1.0.0";

    private static final String INVALID_FLOW_DEFINITION_SAME_NAME_ERROR = "invalid-import-base-flow-same-name-1.0.1";

    private static final String INVALID_FLOW_DEFINITION_FILE = "invalid-flow-definition-file-1.0.0";

    private static final String INVALID_FLOW_PACKAGE_EMPTY = "invalid-flow-empty";

    private static final String INVALID_FLOW_VERSION = "invalid-flow-version-1.0.0";

    @Before
    public void before() {
        importFlow(FLOW_ID, getFlowPackageBytes(IMPORT_BASE_FLOW_1_0_1));
    }

    @Test
    public void test_importFlow_FLOW_PACKAGE_NOT_FOUND() {
        checkFlowFunctionFails(() -> importFlow(INVALID_FLOW_WITHOUT_DEFINITION, null), FLOW_PACKAGE_NOT_FOUND);
    }

    @Test
    public void test_importFlow_FLOW_PACKAGE_EMPTY() {
        checkFlowFunctionFails(() ->
                        importFlow("", getFlowPackageBytes(INVALID_FLOW_PACKAGE_EMPTY)),
                FlowPackageErrorCode.FLOW_PACKAGE_EMPTY);
    }

    @Test
    public void test_importFlow_FLOW_DEFINITION_NOT_FOUND() {
        checkFlowFunctionFails(() ->
                        importFlow(INVALID_FLOW_WITHOUT_DEFINITION, getFlowPackageBytes(INVALID_FLOW_WITHOUT_DEFINITION)),
                FLOW_DEFINITION_NOT_FOUND);
    }


    @Test
    public void test_importFlow_FLOW_DEFINITION_PARSE_ERROR() {
        checkFlowFunctionFails(() ->
                        importFlow(FLOW_ID_INVALID_FLOW_DEFINITION_PARSE_ERROR, getFlowPackageBytes(INVALID_FLOW_DEFINITION_PARSE_ERROR)),
                FLOW_DEFINITION_PARSE_ERROR);
    }

    @Test
    public void test_importFlow_FLOW_VERSION_SYNTAX_INVALID() {
        checkFlowFunctionFails(() ->
                        importFlow(FLOW_ID_INVALID_FLOW_VERSION, getFlowPackageBytes(INVALID_FLOW_VERSION)),
                FLOW_VERSION_SYNTAX_INVALID);
    }

    @Test
    public void test_importFlow_FLOW_VERSION_NOT_ALLOWED() {
        checkFlowFunctionFails(() ->
                        importFlow(FLOW_ID, getFlowPackageBytes(INVALID_FLOW_PACKAGE_WITH_LOWER_VERSION)),
                FLOW_VERSION_NOT_ALLOWED);
    }

    @Test
    public void test_importFlow_FLOW_DEFINITION_INVALID() {
        checkFlowFunctionFails(() ->
                        importFlow(FLOW_ID_INVALID_FLOW_DEFINITION_FILE, getFlowPackageBytes(INVALID_FLOW_DEFINITION_FILE)),
                FLOW_DEFINITION_INVALID);
    }

    @Test
    public void test_importFlow_FLOW_NAME_ALREADY_INUSE() {
        checkFlowFunctionFails(() ->
                        importFlow(FLOW_ID_INVALID_FLOW_DEFINITION_SAME_NAME_ERROR, getFlowPackageBytes(INVALID_FLOW_DEFINITION_SAME_NAME_ERROR)),
                FLOW_NAME_ALREADY_INUSE);
    }

    @Test
    public void test_importFlow_FLOW_ALREADY_EXISTS() {
        checkFlowFunctionFails(() ->
                        importFlow(FLOW_ID, getFlowPackageBytes(IMPORT_BASE_FLOW_1_0_1)),
                FLOW_ALREADY_EXISTS);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }
}
