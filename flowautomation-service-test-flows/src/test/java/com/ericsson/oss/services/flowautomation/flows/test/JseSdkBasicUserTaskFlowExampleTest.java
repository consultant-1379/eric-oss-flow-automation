/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowdataFileBytes;

/**
 * Test cases for SDK Basic User Task Example flow.
 */
public class JseSdkBasicUserTaskFlowExampleTest extends FlowAutomationBaseTest {

    String flowPackage = "sdkBasicUserTaskFlowExample";
    String flowId = "com.ericsson.oss.fa.flows.sdkBasicUserTaskFlowExample";
    FlowDefinition flowDefinition;

    private final String textGroup1Path = "Grouping and Nesting > Nesting > First level of nesting for text inputs > Text Inputs Group 1 > %s";
    private final String textGroup2Path = "Grouping and Nesting > Nesting > First level of nesting for text inputs > Text Inputs Group 2 > %s";
    private final String checkBoxGroup1Path = "Grouping and Nesting > Nesting > First level of nesting for checkboxes > Checkboxes Group 1 > %s";
    private final String checkBoxGroup2Path = "Grouping and Nesting > Nesting > First level of nesting for checkboxes > Checkboxes Group 2 > %s";

    @Before
    public void before() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @After
    public void after() {
        removeFlow(flowId);
    }

    @Test
    public void happyPathInteractiveMode() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);
        checkExecutionIsActive(flowExecution);

        runSetupPhase(flowExecution);
        runExecutePhase(flowExecution);
    }

    @Test
    public void happyPathNonInteractiveMode() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowExecution);

        // ==== Setup phase ====
        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup.zip"));
        completeReviewAndConfirmUserTask(flowExecution);

        runExecutePhase(flowExecution);
    }

    private void runSetupPhase(FlowExecution flowExecution) {
        completeUsertaskChooseSetupInteractive(flowExecution);
        completeInputValidationUserTask(flowExecution);
        completeReadOnlyUserTask(flowExecution);
        completeStandaloneSchemaUserTask(flowExecution);
        completeSelectDynamicObjectsUserTask(flowExecution);
        completeDynamicObjectsUserTask(flowExecution);
        completeGroupingAndNestingUserTask(flowExecution);
        completeReviewAndConfirmUserTask(flowExecution);
    }

    private void runExecutePhase(FlowExecution flowExecution) {
        checkUsertaskActive(flowExecution, "Basic User Task");

        completeUsertask(flowExecution, "Basic User Task", new UsertaskInputBuilder().
                input("Basic User Task > Check box", true).
                input("Basic User Task > List box", "Item 1").
                input("Basic User Task > User Tasks - Nesting > Checkbox 1 level 1 exposes more detail", "true").
                input("Basic User Task > User Tasks - Nesting > Checkbox 1 level 2 exposes more detail", "true").
                input("Basic User Task > User Tasks - Nesting > Text input 1", "test").
                input("Basic User Task > User Tasks - Grouping > Text input 1", "test").
                input("Basic User Task > User Tasks - Grouping > Text input 2", "test"));

        checkUsertaskActive(flowExecution, "Simple User Task");

        checkUsertask(flowExecution, "Simple User Task", new UsertaskCheckBuilder().
                check("Simple User Task > Note: First field is lower case and Second field is upper case. Values must match in both fields. Flow will validate it").
                check("Simple User Task > Text input 1 with default value", "test").
                check("Simple User Task > Text input 2 with default value", "TEST"));

        completeUsertask(flowExecution, "Simple User Task", new UsertaskInputBuilder().
                input("Simple User Task > Text input 1 with default value", "hello").
                input("Simple User Task > Text input 2 with default value", "HELLO"));

        checkUsertaskActive(flowExecution, "Names of the Nodes");

        completeUsertask(flowExecution, "Names of the Nodes",
                new UsertaskInputBuilder().input("Names of the Nodes > First Node Name", "Node 5")
                        .input("Names of the Nodes > Second Node Name", "Node 7")
                        .input("Names of the Nodes > Third Node Name", "Node 9"));

        completeUsertaskNoInput(flowExecution, "Review Script Failure - Exit Code 1 - Node 5");
        completeUsertaskNoInput(flowExecution, "Review Script Failure - Exit Code 2 - Node 7");
        completeUsertaskNoInput(flowExecution, "Review Script Failure - Exit Code 2 - Node 9");

        // ==== Flow execution completed ====
        checkExecutionExecuted(flowExecution);
    }

    private void completeReadOnlyUserTask(FlowExecution flowExecution) {
        checkUsertaskActive(flowExecution, "Read-Only");

        List<String> emailIds = Arrays.asList("user1@ericsson.com", "user2@ericsson.com");
        List<String> listValues = Arrays.asList("Read Only List Value 1", "Read Only List Value 2", "Read Only List Value 3", "Read Only List Value 4");

        //TODO We shouldn't have to supply values for read-only properties
        // For now, if we don't pass input for emails and select-lists, it will throw an Exception.
        // We need to supply the same values that are bound in the schema. The check in the review and confirm
        // user task will be performed against the bound values. If we pass different values, the check will fail.
        completeUsertask(flowExecution, "Read-Only", new UsertaskInputBuilder()
                .input("Read-Only > Read Only List", listValues)
                .input("Read-Only > Send email notifications to (comma separated email addresses):", emailIds));
    }

    private void completeGroupingAndNestingUserTask(FlowExecution flowExecution) {
        checkUsertaskActive(flowExecution, "Grouping and Nesting");

        completeUsertask(flowExecution, "Grouping and Nesting", new UsertaskInputBuilder()
                .input(String.format(textGroup1Path, "Text Input 1"), "group1Text1")
                .input(String.format(textGroup1Path, "Text Input 2"), "group1Text2")
                .input(String.format(textGroup2Path, "Text Input 1"), "group2Text1")
                .input(String.format(textGroup2Path, "Text Input 2"), "group2Text2")
                .input(String.format(checkBoxGroup1Path, "Checkbox 1"), true)
                .input(String.format(checkBoxGroup1Path, "Checkbox 2"), true)
                .input(String.format(checkBoxGroup2Path, "Checkbox 1"), true)
                .input(String.format(checkBoxGroup2Path, "Checkbox 2"), true)
                .input("Grouping and Nesting > Nesting with checkbox > Select Option: > Option 1", true)
                .input("Grouping and Nesting > Nesting with checkbox > Select Option: > Option 2", true)
                .input("Grouping and Nesting > Nesting with checkbox > Select Option: > Other > Description", "nestedCheckBoxValue")
                .input("Grouping and Nesting > Nesting with radio button > Select option: > Option with nesting > Nested Text Input", "nestedRadioValue"));
    }

    private void completeDynamicObjectsUserTask(FlowExecution flowExecution) {
        checkUsertaskActive(flowExecution, "Dynamic Objects");
        completeUsertask(flowExecution, "Dynamic Objects", new UsertaskInputBuilder()
                .input("Dynamic Objects > Software Package > Software Package Name", "name1")
                .input("Dynamic Objects > Software Package > Software Package Version", "version1")
                .input("Dynamic Objects > Network > Network", "network1")
                .input("Dynamic Objects > Network > Subnetwork", "subnetwork1"));
    }

    private void completeSelectDynamicObjectsUserTask(FlowExecution flowExecution) {
        checkUsertaskActive(flowExecution, "Select Dynamic Objects");
        completeUsertask(flowExecution, "Select Dynamic Objects", new UsertaskInputBuilder()
                .input("Select Dynamic Objects > Software Package", true)
                .input("Select Dynamic Objects > Network", true));
    }

    private void completeStandaloneSchemaUserTask(FlowExecution flowExecution) {
        checkUsertaskActive(flowExecution, "User Task Using Standalone Schema In Setup Phase");
        completeUsertask(flowExecution, "User Task Using Standalone Schema In Setup Phase", new UsertaskInputBuilder()
                .input("User Task Using Standalone Schema In Setup Phase > Text Input", "standalone"));
    }

    private void completeInputValidationUserTask(FlowExecution flowExecution) {
        checkUsertaskActive(flowExecution, "Input Validation");
        completeUsertask(flowExecution, "Input Validation", new UsertaskInputBuilder()
                .input("Input Validation > Node Name", "node1")
                .input("Input Validation > Select box", "SHORT"));
    }

    private void completeReviewAndConfirmUserTask(FlowExecution flowExecution) {
        List<String> emailIds = Arrays.asList("user1@ericsson.com", "user2@ericsson.com");
        List<String> listValues = Arrays.asList("Read Only List Value 1", "Read Only List Value 2", "Read Only List Value 3", "Read Only List Value 4");

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder()
                .check("Input Validation > Text input with default value", "placeholder")
                .check("Input Validation > Node Name", "node1")
                .check("Input Validation > Select box", "Short contents")
                .check("Read-Only > Derived Flow Input Data", "placeholder + content added by the flow")
                .check("Read-Only > Short Information", "This is short information")
                .check("Read-Only > Derived Date Time", "2022-01-04T11:22:33.000Z")
                .check("Read-Only > Read Only Table", getExpectedReadOnlyTableData())
                .check("Read-Only > Read Only List", listValues)
                .check("Read-Only > Send email notifications to (comma separated email addresses):", emailIds)
                .check("Software Package > Software Package Name", "name1")
                .check("Software Package > Software Package Version", "version1")
                .check("Network > Network", "network1")
                .check("Network > Subnetwork", "subnetwork1")
                .check(String.format(textGroup1Path, "Text Input 1"), "group1Text1")
                .check(String.format(textGroup1Path, "Text Input 2"), "group1Text2")
                .check(String.format(textGroup2Path, "Text Input 1"), "group2Text1")
                .check(String.format(textGroup2Path, "Text Input 2"), "group2Text2")
                .check(String.format(checkBoxGroup1Path, "Checkbox 1"), true)
                .check(String.format(checkBoxGroup1Path, "Checkbox 2"), true)
                .check(String.format(checkBoxGroup2Path, "Checkbox 1"), true)
                .check(String.format(checkBoxGroup2Path, "Checkbox 2"), true)
                .check("Grouping and Nesting > Nesting with checkbox > Select Option: > Option 1", true)
                .check("Grouping and Nesting > Nesting with checkbox > Select Option: > Option 2", true)
                .check("Grouping and Nesting > Nesting with checkbox > Select Option: > Other > Description", "nestedCheckBoxValue")
                .check("Grouping and Nesting > Nesting with radio button > Select option: > Option with nesting > Nested Text Input", "nestedRadioValue"));

        completeUsertaskReviewAndConfirm(flowExecution);
    }

    private List<Map<String, Object>> getExpectedReadOnlyTableData() {
        Map<String, Object> inlineMap1 = new LinkedHashMap<>();
        inlineMap1.put("packageName", "Package0");
        inlineMap1.put("productName", "Product0");
        inlineMap1.put("productId", 0);
        inlineMap1.put("packageTitle", "Test package title0");
        inlineMap1.put("nodeDistance", 0);
        inlineMap1.put("nodeType", "Node Type0");
        inlineMap1.put("customDetails", "Derived Data generated by the flow0");

        Map<String, Object> inlineMap2 = new LinkedHashMap<>();
        inlineMap2.put("packageName", "Package1");
        inlineMap2.put("productName", "Product1");
        inlineMap2.put("productId", 1);
        inlineMap2.put("packageTitle", "Test package title1");
        inlineMap2.put("nodeDistance", 1);
        inlineMap2.put("nodeType", "Node Type1");
        inlineMap2.put("customDetails", "Derived Data generated by the flow1");

        return Stream.of(inlineMap1, inlineMap2).collect(Collectors.toList());
    }
}
