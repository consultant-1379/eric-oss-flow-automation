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
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_INPUT;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_NAME_IN_USE;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_VALIDATION_FAILED;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_INPUT_SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_PROCESSING_ERROR;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionInput;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

public abstract class ExecuteFlowErrorTest extends FlowAutomationNegativeTest {

    private static final String FLOW_ID = "com.ericsson.oss.fa.flows.helloWorld";

    private static final String FLOW_ID_THAT_DOES_NOT_EXIST = "com.ericsson.oss.fa.flows.interfacetest.testBaseFlow-two";

    private static final String FLOW_ID_MISSING_INPUT = "com.ericsson.oss.fa.flows.helloWorldMissingInput";

    private static final String IMPORT_HELLO_WORLD = "helloWorld-1.0.1";

    private static final String IMPORT_HELLO_WORLD_MISSING_SCHEMA = "helloWorld-missing-input-1.0.1";

    private static final String INVALID_FLOW_INPUT_JSON = "flowInput.json";

    private static final String INVALID_EMPTY_FLOW_INPUT_SETUP = "invalid-flow-data-1.0.1";

    private static final String INVALID_JSON_FLOW_INPUT_SETUP = "invalid-json-flow-data-1.0.1";

    private static final String INVALID_NO_FLOW_INPUT_SETUP = "flowdata/invalid-no-flow-data-1.0.1";

    FlowDefinition flowDefinition;

    FlowDefinition flowDefinitionMissingInput;

    @Before
    public void before() {
        flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(IMPORT_HELLO_WORLD));
    }

    @Test
    public void test_execute_FLOW_DOES_NOT_EXIST() {
        flowDefinition = new FlowDefinition(FLOW_ID_THAT_DOES_NOT_EXIST, flowDefinition.getName(), flowDefinition.getStatus(),
                flowDefinition.getSource(), flowDefinition.getFlowVersions());
        String executionName = createUniqueInstanceName(FLOW_ID_THAT_DOES_NOT_EXIST);
        checkFlowFunctionFails(() -> flowService.executeFlow(FLOW_ID_THAT_DOES_NOT_EXIST, executionName), FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_execute_FLOW_EXECUTION_NAME_IN_USE() {
        String executionName = createUniqueInstanceName(FLOW_ID);
        startFlowExecution(flowDefinition, executionName);
        checkFlowFunctionFails(() -> flowService.executeFlow(FLOW_ID, executionName), FLOW_EXECUTION_NAME_IN_USE);
    }

    @Test
    public void test_execute_INVALID_FLOW_INPUT() {
        String executionName = createUniqueInstanceName(FLOW_ID);
        final FlowExecutionInput flowExecutionInput = new FlowExecutionInput(executionName, INVALID_FLOW_INPUT_JSON,
                getFlowExecutionInput(INVALID_NO_FLOW_INPUT_SETUP, INVALID_FLOW_INPUT_JSON));
        checkFlowFunctionFails(() -> flowService.executeFlow(FLOW_ID, flowExecutionInput), INVALID_FLOW_INPUT);

    }

    @Test
    public void test_execute_JSON_VALIDATION_FAILED() {
        String executionName = createUniqueInstanceName(FLOW_ID);
        checkFlowFunctionFails(() -> {
            startFlowExecutionWithFile(flowDefinition, executionName, INVALID_FLOW_INPUT_JSON, getFlowdataFileBytes(INVALID_EMPTY_FLOW_INPUT_SETUP,
                    "flow-input.json"));
        }, JSON_VALIDATION_FAILED);
    }

    @Test
    public void test_execute_FLOW_INPUT_SCHEMA_NOT_FOUND() {
        flowDefinitionMissingInput = importFlow(FLOW_ID_MISSING_INPUT, getFlowPackageBytes(IMPORT_HELLO_WORLD_MISSING_SCHEMA));
        String executionName = createUniqueInstanceName(FLOW_ID_MISSING_INPUT);
        checkFlowFunctionFails(() -> {
            startFlowExecutionWithFile(flowDefinitionMissingInput, executionName, INVALID_FLOW_INPUT_JSON, getFlowdataFileBytes(INVALID_EMPTY_FLOW_INPUT_SETUP,
                    "flow-input.json"));
        }, FLOW_INPUT_SCHEMA_NOT_FOUND);
        removeFlow(FLOW_ID_MISSING_INPUT);

    }

    @Test
    public void test_execute_JSON_PROCESSING_ERROR() {
        String executionName = createUniqueInstanceName(FLOW_ID);
        final FlowExecutionInput flowExecutionInput = new FlowExecutionInput(executionName, INVALID_FLOW_INPUT_JSON,
                getFlowExecutionInput(INVALID_JSON_FLOW_INPUT_SETUP, INVALID_FLOW_INPUT_JSON));
        checkFlowFunctionFails(() -> flowService.executeFlow(FLOW_ID, flowExecutionInput), JSON_PROCESSING_ERROR);

    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
    }

    private Map<String, byte[]> getFlowExecutionInput(String fileLocation, String fileName) {
        final byte[] flowPackage = getFlowPackageBytes(fileLocation);
        final Map<String, byte[]> flowInputFile = new HashMap<>();
        flowInputFile.put(fileName, flowPackage);
        return flowInputFile;
    }
}