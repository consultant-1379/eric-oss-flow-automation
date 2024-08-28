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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.realSleepMs;
import static com.jayway.jsonassert.JsonAssert.with;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Download Flow Execution Resources Test cases.
 * It uses the multiInstance-loadControl flow as it already has a report and flowInput.
 */
public abstract class FlowExecutionResourceDownloadTest extends FlowAutomationBaseTest {

    int numElements = 2;

    String flowPackage = "multiInstance-loadControl-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.multiInstanceLoadControl";
    FlowDefinition flowDefinition;
    private String setupFolder = "setup/flow-input.json";
    private String reportFileName = "-report.xlsx";
    private  static  final String EXPECTED_BEHAVIOUR = "Expected behaviour {}";

    private static final Logger logger = LoggerFactory.getLogger(FlowExecutionResourceDownloadTest.class);

    @Before
    public void before() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @After
    public void after() {
        removeFlow(flowId);
    }

    @Test
    public void testEndToEndFlowExecution() throws IOException {
        //Start the flow execution and select interactive mode
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);

        //Assertions for setup phase, nothing is available to download
        checkReportNotAvailableToDownload(flowExecution);
        checkFlowInputNotAvailableToDownload(flowExecution);
        checkAllResourcesNotAvailableToDownload(flowExecution);

        //Build and complete the usertask
        UsertaskInputBuilder usertaskInputBuilder = new UsertaskInputBuilder();
        usertaskInputBuilder.input("Configuration > Number of Elements", ""+ numElements);
        usertaskInputBuilder.input("Configuration > Load Control Pool Size", "4");
        usertaskInputBuilder.input("Configuration > Instance Sleep in Seconds", "10");
        usertaskInputBuilder.input("Configuration > Generate incidents", false);
        completeUsertask(flowExecution, "Configuration", usertaskInputBuilder);

        // Assertions for review and confirm phase,

        // report wont be available to download.
        checkReportNotAvailableToDownload(flowExecution);
        // Lets wait a sec to before trying to get the file
        realSleepMs(1000);
        //flowInput will be available to download.
        FlowExecutionResource flowInputResource = downloadFlowExecutionInput(flowExecution);
        assert (flowInputResource.getName().contains(".zip"));
        final Predicate<ZipEntry> flowInputPredicate = zipEntry -> zipEntry.getName().equals(setupFolder);
        Optional<byte[]> bytes = FlowPackageUtil.extractFile(flowInputResource.getData(), flowInputPredicate);
        assertTrue(bytes.isPresent());
        if(bytes.isPresent()) {
            checkFlowInput(bytes.get());
        }

        //Check that the all resources zip contains only the flowInput inside it.
        FlowExecutionResource allResources = downloadFlowExecutionAllResources(flowExecution);
        assertEquals(flowExecution.getName()+"-resources.zip", allResources.getName());
        checkAllResourcesContainsOnlyFlowInput(allResources.getData());

        //Review and Confirm
        completeUsertaskReviewAndConfirm(flowExecution);

        //Check Execution
        checkExecutionExecuted(flowExecution);
        checkExecutionSummaryReport(flowExecution, "Our work is done here");
        checkExecutionIsInactive(flowId, executionName);

        // Check report
        FlowExecutionResource reportResource = downloadFlowExecutionReport(flowExecution);
        assertEquals(flowExecution.getName()+reportFileName, reportResource.getName());
        assert (reportResource.getName().contains(".xlsx"));


        //Check flowInput
        flowInputResource = downloadFlowExecutionInput(flowExecution);
        bytes = FlowPackageUtil.extractFile(flowInputResource.getData(), flowInputPredicate);
        assertTrue(bytes.isPresent());
        if(bytes.isPresent()) {
            checkFlowInput(bytes.get());
        }

        //Check Zip of All resources
        allResources = downloadFlowExecutionAllResources(flowExecution);
        assertEquals(flowExecution.getName()+"-resources.zip", allResources.getName());
        checkAllResources(allResources.getData());
    }

    private void checkFlowInput(byte[] flowInputBytes) {
        String flowInput = new String(flowInputBytes, StandardCharsets.UTF_8);
        with(flowInput).assertThat("$.configuration.numElements", equalTo("" + numElements)).
                assertThat("$.configuration.loadControlPoolSize", equalTo("4")).
                assertThat("$.configuration.instanceSleepSeconds", equalTo("10")).
                assertThat("$.configuration.generateIncidents", equalTo(false));
    }

    @SuppressWarnings({"squid:S3655"})
    private void checkAllResources(byte[] resourcesZipBytes) {
        try {
            final Predicate<ZipEntry> reportPredicate = zipEntry -> zipEntry.getName().contains(reportFileName);
            Optional<byte[]> reportBytes = FlowPackageUtil.extractFile(resourcesZipBytes, reportPredicate);
            assertTrue(reportBytes.isPresent());
            final Predicate<ZipEntry> flowInputPredicate = zipEntry -> zipEntry.getName().equals(setupFolder);
            checkFlowInput(FlowPackageUtil.extractFile(resourcesZipBytes, flowInputPredicate).get());
        } catch (IOException e) {
            logger.error("Failed to extract the flow resourcesZipBytes from the zip file: {}", e.getMessage());
            fail("Failed to extract the flow resourcesZipBytes from the zip file: " + e.getMessage());
        }
    }

    @SuppressWarnings({"squid:S3655"})
    private void checkAllResourcesContainsOnlyFlowInput(byte[] resourcesZipBytes) {
        try {
            final Predicate<ZipEntry> reportPredicate = zipEntry -> zipEntry.getName().contains(reportFileName);
            final Optional<byte[]> reportBytes = FlowPackageUtil.extractFile(resourcesZipBytes, reportPredicate);
            assertFalse(reportBytes.isPresent()); //Assert that the report won't be available inside the zip

            final Predicate<ZipEntry> flowInputPredicate = zipEntry -> zipEntry.getName()
                    .equals(setupFolder);
            checkFlowInput(FlowPackageUtil.extractFile(resourcesZipBytes, flowInputPredicate).get());
        } catch (IOException e) {
            logger.error("Failed to extract the flow resourcesZipBytes from the zip file: {}", e.getMessage());
            fail("Failed to extract the flow resourcesZipBytes from the zip file: " + e.getMessage());
        }
    }

    @SuppressWarnings({"squid:S00108"})
    private void checkAllResourcesNotAvailableToDownload(FlowExecution flowExecution) {


        try {
            downloadFlowExecutionAllResources(flowExecution);
            fail("Expected the Exception as execution resources zip cannot be available for download at this stage.");
        }
        catch (FlowAutomationException e) {
            logger.trace(EXPECTED_BEHAVIOUR, e.getMessage());
        }
    }

    @SuppressWarnings({"squid:S00108"})
    private void checkFlowInputNotAvailableToDownload(FlowExecution flowExecution) {
        try {
            downloadFlowExecutionInput(flowExecution);
            fail("Expected the Exception as flowinput cannot be available for download at this stage.");
        }
        catch (FlowAutomationException e) {
            logger.trace(EXPECTED_BEHAVIOUR, e.getMessage());
        }
    }

    @SuppressWarnings({"squid:S00108"})
    private void checkReportNotAvailableToDownload(FlowExecution flowExecution) {
        try {
            downloadFlowExecutionReport(flowExecution);
            fail("Expected the Exception as report cannot be available for download at this stage.");
        }
        catch (FlowAutomationException e) {
            logger.trace(EXPECTED_BEHAVIOUR, e.getMessage());
        }
    }
}
