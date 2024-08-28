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

import static com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity.ERROR;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Test cases for Variable and Data Example flow.
 */
public class JseVariableAndDataShowcaseTest extends FlowAutomationBaseTest {

    String flowPackage = "variableAndDataShowcase";
    String flowId = "com.ericsson.oss.fa.flows.variableAndDataShowcase";
    FlowDefinition flowDefinition;

    @Before
    public void before() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @After
    public void after() {
        removeFlow(flowId);
    }

    @Test
    public void testInteractiveSetup() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowExecution);

        completeUsertaskChooseSetupInteractive(flowExecution);

        checkUsertaskActive(flowExecution, "Input Data");

        completeUsertask(flowExecution, "Input Data", new UsertaskInputBuilder().
                input("Input Data > Boolean Input", true).
                input("Input Data > Integer Input", 1).
                input("Input Data > String Input", "foo").
                input("Input Data > Date Time Input", "2020-01-27T22:33:44.000Z").
                input("Input Data > File Input", "file.json", getFlowdataFileBytes(flowPackage, "file.json")));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check("Input Data > Boolean Input", true).
                check("Input Data > Integer Input", 1).
                check("Input Data > String Input", "foo").
                check("Input Data > Date Time Input", "2020-01-27T22:33:44.000Z").
                check("Input Data > File Input", "file.json"));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);
    }

    @Test
    public void testFileInputSetup() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowExecution);

        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup.zip"));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check("Input Data > Boolean Input", true).
                check("Input Data > Integer Input", 1).
                check("Input Data > String Input", "foo").
                check("Input Data > Date Time Input", "2020-01-27T22:33:44.000Z").
                check("Input Data > File Input", "file.json"));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);
    }

    @Test
    public void testFileInputSetupInvalidMissingData() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowExecution);

        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup-invalid-missing-data.zip"));

        checkExecutionEventIsRecorded(flowExecution, ERROR, "The file must contain a JSON object with 'foo' key");

        checkExecutionFailedSetup(flowExecution);
    }

}
