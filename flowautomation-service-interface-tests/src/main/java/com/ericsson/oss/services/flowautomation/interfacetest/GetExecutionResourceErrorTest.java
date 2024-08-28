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
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.NO_FLOW_EXECUTION_RESOURCE_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SETUP_PHASE_IN_PROGRESS;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_RESOURCE_DOWNLOAD_REQUEST;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import com.ericsson.oss.services.flowautomation.interfacetest.fwk.FlowAutomationNegativeTest;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class GetExecutionResourceErrorTest extends FlowAutomationNegativeTest {

    private static final String FLOW_ID = "com.ericsson.oss.fa.flows.helloWorld";

    private static final String FLOW_WITHOUT_RESOURCES_ID = "com.ericsson.oss.fa.flows.without-resources";

    private static final String IMPORT_HELLO_WORLD = "helloWorld-1.0.1";

    private static final String IMPORT_MULTI_INSTANCE = "multi-instance-download-variable-1.0.1";

    private static final String FLOW_ID_MULTI_INSTANCE = "com.ericsson.oss.fa.flows.multiInstance.download-variables";

    private static FlowDefinition flowDefinition;
    private static FlowDefinition flowDefinitionMultiInstanceDownloadVariable;

    @Before
    public void before() {
        flowDefinition = importFlow(FLOW_ID, getFlowPackageBytes(IMPORT_HELLO_WORLD));
        flowDefinitionMultiInstanceDownloadVariable = importFlow(FLOW_ID_MULTI_INSTANCE, getFlowPackageBytes(IMPORT_MULTI_INSTANCE));
    }

    @Test
    public void test_downloadExecutionResource_FLOW_EXECUTION_NOT_FOUND() {
        String executionName = createUniqueInstanceName(FLOW_ID);
        startFlowExecution(flowDefinition, executionName);
        checkFlowFunctionFails(() -> flowExecutionService.getExecutionResource(FLOW_ID, "None", "all"),
                FLOW_EXECUTION_NOT_FOUND);
    }

    @Test
    public void test_downloadExecutionResource_FLOW_DOES_NOT_EXIST() {
        String executionName = createUniqueInstanceName(FLOW_ID);
        startFlowExecution(flowDefinition, executionName);
        checkFlowFunctionFails(() -> flowExecutionService.getExecutionResource("None", executionName, "all"),
                FLOW_DOES_NOT_EXIST);
    }

    @Test
    public void test_downloadExecutionResource_SETUP_PHASE_IN_PROGRESS() {
        String executionName = createUniqueInstanceName(FLOW_ID_MULTI_INSTANCE);
        FlowExecution flowExecution = startFlowExecution(flowDefinitionMultiInstanceDownloadVariable, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);
        checkExecutionState(flowExecution, "SETTING_UP");
        checkFlowFunctionFails(() -> flowExecutionService.getExecutionResource(FLOW_ID_MULTI_INSTANCE, executionName, "report"),
                SETUP_PHASE_IN_PROGRESS);

    }

    @Test
    public void test_downloadExecutionResource_INVALID_RESOURCE_DOWNLOAD_REQUEST() {
        String executionName = createUniqueInstanceName(FLOW_ID_MULTI_INSTANCE);
        checkFlowFunctionFails(() -> flowExecutionService.getExecutionResource(FLOW_ID_MULTI_INSTANCE, executionName, "invalidResource"),
                INVALID_RESOURCE_DOWNLOAD_REQUEST);
    }

    @Test
    public void test_downloadExecutionResource_NO_FLOW_EXECUTION_RESOURCE_AVAILABLE() {
        flowDefinition = importFlow(FLOW_WITHOUT_RESOURCES_ID, getFlowPackageBytes("flow-without-resources-1.0.0"));
        final String executionName = createUniqueInstanceName(FLOW_WITHOUT_RESOURCES_ID);
        final FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkFlowFunctionFails(() -> flowExecutionService.getExecutionResource(FLOW_WITHOUT_RESOURCES_ID, executionName, "all"),
                NO_FLOW_EXECUTION_RESOURCE_AVAILABLE);

        removeFlow(FLOW_WITHOUT_RESOURCES_ID);
    }

    @After
    public void after() {
        removeFlow(FLOW_ID);
        removeFlow(FLOW_ID_MULTI_INSTANCE);
    }


}