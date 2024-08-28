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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;
import static com.jayway.jsonassert.JsonAssert.with;

import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Multi-instance load control flow test cases
 */
public abstract class ReportVariableDownloadTest extends FlowAutomationBaseTest {

    public static final String REPORT_VARIABLE_DOWNLOAD_URL = "/flow-automation/v1/executions/%s/download/report-variable/%s?flow-id=%s";
    public static final String UPLOADED_FILE_CONTENT = "uploadedFileContent";
    public static final String UPLOADED_FILE_CONTENT1 = "uploadedFileContent";
    String flowPackage = "multi-instance-download-variable-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.multiInstance.download-variables";

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
    @SuppressWarnings("squid:S1126")
    public void testEndToEndFlowExecution() {
        @SuppressWarnings({"squid:S00117"}) final int NUM_ELEMENTS = 2;

        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        UsertaskInputBuilder usertaskInputBuilder = new UsertaskInputBuilder();
        usertaskInputBuilder.input("Configuration > Number of Elements", "" + NUM_ELEMENTS);
        usertaskInputBuilder.input("Configuration > Load Control Pool Size", "2");
        usertaskInputBuilder.input("Configuration > Instance Sleep in Seconds", "1");
        usertaskInputBuilder.input("Configuration > Generate incidents", false);

        completeUsertask(flowExecution, "Configuration", usertaskInputBuilder);
        completeUsertask(flowExecution, "Upload File", new UsertaskInputBuilder().
                input("Upload File > File", "file.txt", getFlowdataFileBytes(flowPackage, "file.txt")));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);
        checkExecutionReport(flowExecution);

        FlowExecutionResource allResources = downloadFlowExecutionReport(flowExecution);
        assertEquals(flowExecution.getName()+"-report.xlsx", allResources.getName());

        // Check report
        String report = getExecutionReport(flowExecution);
        with(report).assertThat("$.header.status", equalTo("COMPLETED"));
        with(report).assertThat("$.body.numElements", equalTo(NUM_ELEMENTS)).
                assertThat("$.body.numElementsOngoing", equalTo(0)).
                assertThat("$.body.numElementsQueued", equalTo(0)).
                assertThat("$.body.numElementsCompleted", equalTo(NUM_ELEMENTS)).
                assertThat("$.body.fileContent", equalTo("helloworld")).
                assertThat("$.body.uploadedFileContent", equalTo(String.format(REPORT_VARIABLE_DOWNLOAD_URL, flowExecution.getName(), UPLOADED_FILE_CONTENT, flowExecution.getFlowId())));
        downloadAndCheckReportVariableContentContains(flowExecution, UPLOADED_FILE_CONTENT, "hello");

        with(report).assertThat("$.body.elementsTable.length()", equalTo(NUM_ELEMENTS));
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            String variableName = "element" + i + ".kpiContent";
            String expectedDownloadUrl = String.format(REPORT_VARIABLE_DOWNLOAD_URL, flowExecution.getName(), variableName, flowExecution.getFlowId());
            with(report).assertThat("$.body.elementsTable[" + i + "].kpiContent", equalTo(expectedDownloadUrl));
            downloadAndCheckReportVariableContentContains(flowExecution,variableName, "This is dummy data for element"+i);
        }
    }

    @Test
    public void testEndToEndWithLargeVariable() {

        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        completeUsertaskChooseSetupInteractive(flowExecution);

        UsertaskInputBuilder usertaskInputBuilder = new UsertaskInputBuilder();
        usertaskInputBuilder.input("Configuration > Number of Elements", "" + 2);
        usertaskInputBuilder.input("Configuration > Load Control Pool Size", "2");
        usertaskInputBuilder.input("Configuration > Instance Sleep in Seconds", "1");
        usertaskInputBuilder.input("Configuration > Generate incidents", false);

        completeUsertask(flowExecution, "Configuration", usertaskInputBuilder);
        completeUsertask(flowExecution, "Upload File", new UsertaskInputBuilder().
                input("Upload File > File", "largeFile.txt", getFlowdataFileBytes(flowPackage, "largeFile.txt")));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkExecutionExecuted(flowExecution);

        checkExecutionReport(flowExecution);
        // Check report
        String report = getExecutionReport(flowExecution);
        with(report).assertThat("$.header.status", equalTo("COMPLETED"))
        .assertThat("$.body.fileContent", equalTo(""));
        with(report).assertThat("$.body.uploadedFileContent", equalTo(String.format(REPORT_VARIABLE_DOWNLOAD_URL, flowExecution.getName(), UPLOADED_FILE_CONTENT1, flowExecution.getFlowId())));
        downloadAndCheckReportVariableContentContains(flowExecution, UPLOADED_FILE_CONTENT1, "This file contains more than 4Kb data");
    }
}
