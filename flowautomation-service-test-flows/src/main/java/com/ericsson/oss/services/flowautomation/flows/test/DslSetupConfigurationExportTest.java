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
import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil;
import com.ericsson.oss.services.flowautomation.flow.utils.PayloadTransformer;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Test cases for setup configuration export.
 * The flow uses 3 usertasks and the inputs from them are assigned to flowInput,
 * Also the flow assigns some extra data in the flowInput which shouldn't be available in the export and the order of properties in exported flow input should follow the order from flow input schema.
 */
public abstract class DslSetupConfigurationExportTest extends FlowAutomationBaseTest {

    public static final String INPUT = "Input";
    String flowPackage = "setup-configuration-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.setup-configuration";
    FlowDefinition flowDefinition;

    int numInitialFlowsImported;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        numInitialFlowsImported = getNumberFlowsImported();
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
        checkNumberOfFlowsImported(1 + numInitialFlowsImported);
    }

    @Test
    public void testSetupConfigurationExport() throws IOException {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, "User Task 1", new UsertaskInputBuilder().
                input(INPUT, "hello world 1"));

        completeUsertask(flowExecution, "User Task 2", new UsertaskInputBuilder().
                input(INPUT, "hello world 2"));

        completeUsertask(flowExecution, "User Task 3", new UsertaskInputBuilder().
                input(INPUT, "hello world 3"));


        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder());
        exportAndCheckFlowInput(flowExecution);

        completeUsertaskReviewAndConfirm(flowExecution);
    }

    /**
     * We are making sure that the exported flowInput maintains the order of usertasks and contains no extra data.
     * @param flowExecution the flow execution
     */
    private void exportAndCheckFlowInput(FlowExecution flowExecution) throws IOException {
        FlowExecutionResource flowInputResource = downloadFlowExecutionInput(flowExecution);
        byte[] flowInputBytes = FlowPackageUtil.extractFile(flowInputResource.getData(), zipEntry -> zipEntry.getName().equals("setup/flow-input.json"))
                .orElseThrow(() -> new RuntimeException("flow input not downloaded."));
        String flowInput = new String(flowInputBytes, StandardCharsets.UTF_8);

        final Set<String> expectedUsertasksInOrder = Stream.of("usertask1", "usertask2", "usertask3")
                .collect(Collectors.toCollection(LinkedHashSet::new));

        final Set<String> usertasksInExportedSetupConfiguration = PayloadTransformer.transformJsonToMap(flowInput).keySet();

        assertEquals(expectedUsertasksInOrder, usertasksInExportedSetupConfiguration);

        with(flowInput).assertThat("$.usertask1", equalTo("hello world 1")).
                assertThat("$.usertask2", equalTo("hello world 2")).
                assertThat("$.usertask3", equalTo("hello world 3"));
    }


    @After
    public void after() {
        removeFlow(flowId);
        checkNumberOfFlowsImported(numInitialFlowsImported);
    }
}
