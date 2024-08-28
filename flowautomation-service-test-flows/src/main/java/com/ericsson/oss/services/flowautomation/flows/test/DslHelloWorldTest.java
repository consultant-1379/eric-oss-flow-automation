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

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

import com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader;
import com.ericsson.oss.services.flowautomation.model.FlowVersion;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

import java.util.LinkedList;
import java.util.List;

/**
 * Test cases for Hello World flow using a simple DSL.
 */
public abstract class DslHelloWorldTest extends FlowAutomationBaseTest {

    String flowPackage = "helloWorld-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.helloWorld";

    FlowDefinition flowDefinition;
    int numInitialFlowsImported;

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @After
    public void after() {
        removeFlow(flowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }

    @Test
    public void testEndToEndFlowExecutionUsingSetupZip() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup.zip"));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionIsInactive(flowId, executionName);
        
        checkExecutionExecuted(flowExecution);
    }

    @Test
    public void testEndToEndFlowExecutionUsingFlowInputJson() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowId, executionName);

        completeUsertaskChooseSetupFile(flowExecution, "flow-input.json", getFlowdataFileBytes(flowPackage, "flowInput.json"));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionIsInactive(flowId, executionName);

        checkExecutionExecuted(flowExecution);
    }

    @Test
    public void checkFlowInputSchema() {
        String expectedSchema = "{" +
                "  \"$schema\": \"http://json-schema.org/draft-04/schema#\"," +
                "  \"type\": \"object\"," +
                "  \"properties\": {" +
                "    \"message\": {" +
                "      \"type\": \"string\"" +
                "    }" +
                "  }," +
                "  \"required\": [" +
                "    \"message\"" +
                "  ]" +
                "}";

        checkFlowInputSchemaForFlowVersion(flowDefinition, "1.0.1", expectedSchema);
    }

    @Test
    public void checkProcessDetailsForFlowVersion() {
        String processId = "com.ericsson.oss.fa.flows.helloWorld.001.000.001";
        String setupProcessId = "com.ericsson.oss.fa.flows.helloWorld.001.000.001.-.hello-setup";
        String executeProcessId = "com.ericsson.oss.fa.flows.helloWorld.001.000.001.-.hello-execute";
        FlowVersionProcessDetails expectedVersionProcessDetails = new FlowVersionProcessDetails(processId, setupProcessId, executeProcessId);

        checkProcessDetailsForFlowVersion(flowDefinition, "1.0.1", expectedVersionProcessDetails);
    }

    @Test
    public void checkFlowDefinition() {
        String version = "1.0.1";
        String description = "Hello world flow";
        boolean active = true;
        String createdBy = (clientType == TestClientType.JSE) ? "#Ericsson" : "faadmin";
        String createdDate = "createdDate=2021-05-20 10:56:08.0";
        boolean setupPhaseRequired = true;
        boolean reportSupported = true;

        String id = "com.ericsson.oss.fa.flows.helloWorld";
        String name = "Hello World";
        String status = "enabled";
        String source = "EXTERNAL";
        List<FlowVersion> flowVersionsTest = new LinkedList<>();

        flowVersionsTest.add(new FlowVersion(version, description, active, createdBy, createdDate, setupPhaseRequired, reportSupported));
        FlowDefinition expectedDefinition = new FlowDefinition(id, name, status, source, flowVersionsTest);

        checkFlowDefinition(flowId, expectedDefinition);
    }
}
