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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Usertask showcase flow tests
 */
@SuppressWarnings({"squid:S00100"})
public abstract class UsertaskShowcaseTest extends FlowAutomationBaseTest {

    private static final String USERTASK_SELECTION_INFORMATION = "Usertask Selection > Information";
    private static final String USERTASK_SELECTION_READ_ONLY_USER_TASK = "Usertask Selection > Read Only User Task";
    private static final String USERTASK_SELECTION_TEXT_INPUTS = "Usertask Selection > Text Inputs";
    private static final String USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION = "Usertask Selection > Text Inputs With Information";
    private static final String USERTASK_SELECTION_CHECKBOXES = "Usertask Selection > Checkboxes";
    private static final String USERTASK_SELECTION_RADIO_BUTTONS = "Usertask Selection > Radio Buttons";
    private static final String USERTASK_SELECTION_SELECTBOXES = "Usertask Selection > Selectboxes";
    private static final String USERTASK_SELECTION_LISTBOXES = "Usertask Selection > Listboxes";
    private static final String USERTASK_SELECTION_TABLES = "Usertask Selection > Tables";
    private static final String USERTASK_SELECTION_GROUPING_AND_NESTING = "Usertask Selection > Grouping And Nesting";
    private static final String USERTASK_SELECTION_CHECKBOXES_WITH_NESTING = "Usertask Selection > Checkboxes With Nesting";
    private static final String USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING = "Usertask Selection > Radio Buttons With Nesting";
    private static final String USERTASK_SELECTION = "Usertask Selection";
    private static final String GET_EXECUTE_USERTASK_OPTIONS = "Get Execute Usertask Options";
    private static final String GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES = "Get Execute Usertask Options > Number Of Multi-instance Instances";
    private static final String MULTI_INSTANCE_USERTASK_INSTANCE_0 = "Multi-instance Usertask - Instance0";
    private static final String MULTI_INSTANCE_USERTASK_INSTANCE_1 = "Multi-instance Usertask - Instance1";
    private static final String CHECKBOXES_WITH_NESTING_CHECKBOX_2_LEVEL_1_EXPOSES_MORE_DETAIL = "Checkboxes With Nesting > Checkbox 2 level 1 exposes more detail";
    private static final String CHECKBOXES_WITH_NESTING = "Checkboxes With Nesting";
    private static final String RADIO_BUTTONS_WITH_NESTING = "Radio Buttons With Nesting";
    private static final String USERTASK_SELECTION_DATE_TIME_INPUTS = "Usertask Selection > Date Time Inputs";
    private static final String CHECKBOXES_WITH_NESTING_CHECKBOX_LEVEL_1_EXPOSES_MORE_DETAIL_CHECKBOX_LEVEL_2_EXPOSES_MORE_DETAIL_TEXT_INPUT_1 = "Checkboxes With Nesting > Checkbox level 1 exposes more detail > Checkbox level 2 exposes more detail > Text input 1";
    private static final String TEXT_INPUT = "text input";
    private static final String RADIO_BUTTONS_WITH_NESTING_RADIO_GROUP_LABEL_RADIO_EXPOSES_MORE_DETAIL_TEXT_INPUT_1 = "Radio Buttons With Nesting > Radio group label > Radio exposes more detail > Text input 1";
    private static final String TEXT_INPUTS_TEXT_INPUT_WITH_NO_LENGTH_RESTRICTIONS = "Text Inputs > Text input with no length restrictions";
    private static final String TEXT_INPUT_WITH_NO_RESTRICTIONS = "text input with no restrictions";
    private static final String CHECK_BOXES_SHORT_LABEL = "Check Boxes > Short label";
    private static final String CHECK_BOXES_LONG_LABEL_LONG_LABEL_LONG_LABEL_LONG_LABEL = "Check Boxes > Long label long label long label long label";
    private static final String SHORT_CONTENTS = "Short contents";
    private static final String VALUE_2 = "Value 2";
    private static final String PRODUCT_ID = "productId";
    private static final String PRODUCT_NAME = "productName";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PACKAGE_TITLE = "packageTitle";
    private static final String NODE_DISTANCE = "nodeDistance";
    private static final String NODE_TYPE = "nodeType";
    private static final String CUSTOM_DETAILS = "customDetails";
    String flowPackage = "usertaskShowcase-1.0.1";
    String flowId = "com.ericsson.oss.fa.flows.usertaskShowcase";
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
    public void testEndToEndFlowExecution_FileInput() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = doTestEndToEndFlowExecution_FileInput_SetupPhase(executionName);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 0));

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_FileInput_TwoExecuteUsertasks() {
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = doTestEndToEndFlowExecution_FileInput_SetupPhase(executionName);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 2));

        completeUsertaskNoInput(flowExecution, MULTI_INSTANCE_USERTASK_INSTANCE_0);
        completeUsertaskNoInput(flowExecution, MULTI_INSTANCE_USERTASK_INSTANCE_1);

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Interactive_NoSetupUsertasksSelected() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, USERTASK_SELECTION, new UsertaskInputBuilder().
                input(USERTASK_SELECTION_INFORMATION, false).
                input(USERTASK_SELECTION_READ_ONLY_USER_TASK, false).
                input(USERTASK_SELECTION_TEXT_INPUTS, false).
                input(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, false).
                input(USERTASK_SELECTION_CHECKBOXES, false).
                input(USERTASK_SELECTION_RADIO_BUTTONS, false).
                input(USERTASK_SELECTION_SELECTBOXES, false).
                input(USERTASK_SELECTION_LISTBOXES, false).
                input(USERTASK_SELECTION_TABLES, false).
                input(USERTASK_SELECTION_GROUPING_AND_NESTING, false).
                input(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, false).
                input(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, false));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check(USERTASK_SELECTION_INFORMATION, false).
                check(USERTASK_SELECTION_READ_ONLY_USER_TASK, false).
                check(USERTASK_SELECTION_TEXT_INPUTS, false).
                check(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, false).
                check(USERTASK_SELECTION_CHECKBOXES, false).
                check(USERTASK_SELECTION_RADIO_BUTTONS, false).
                check(USERTASK_SELECTION_SELECTBOXES, false).
                check(USERTASK_SELECTION_LISTBOXES, false).
                check(USERTASK_SELECTION_TABLES, false).
                check(USERTASK_SELECTION_GROUPING_AND_NESTING, false).
                check(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, false).
                check(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, false));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 0));

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Interactive_NestedCheckboxesAndRadios_NoneSelected() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, USERTASK_SELECTION, new UsertaskInputBuilder().
                input(USERTASK_SELECTION_INFORMATION, false).
                input(USERTASK_SELECTION_READ_ONLY_USER_TASK, false).
                input(USERTASK_SELECTION_TEXT_INPUTS, false).
                input(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, false).
                input(USERTASK_SELECTION_CHECKBOXES, false).
                input(USERTASK_SELECTION_RADIO_BUTTONS, false).
                input(USERTASK_SELECTION_SELECTBOXES, false).
                input(USERTASK_SELECTION_LISTBOXES, false).
                input(USERTASK_SELECTION_TABLES, false).
                input(USERTASK_SELECTION_GROUPING_AND_NESTING, false).
                input(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, true).
                input(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, true));

        completeUsertask(flowExecution, CHECKBOXES_WITH_NESTING, new UsertaskInputBuilder());

        completeUsertask(flowExecution, RADIO_BUTTONS_WITH_NESTING, new UsertaskInputBuilder());

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 0));

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Interactive_NestedCheckboxesAndRadios_AllSelected() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, USERTASK_SELECTION, new UsertaskInputBuilder().
                input(USERTASK_SELECTION_INFORMATION, false).
                input(USERTASK_SELECTION_READ_ONLY_USER_TASK, false).
                input(USERTASK_SELECTION_TEXT_INPUTS, false).
                input(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, false).
                input(USERTASK_SELECTION_DATE_TIME_INPUTS, false).
                input(USERTASK_SELECTION_CHECKBOXES, false).
                input(USERTASK_SELECTION_RADIO_BUTTONS, false).
                input(USERTASK_SELECTION_SELECTBOXES, false).
                input(USERTASK_SELECTION_LISTBOXES, false).
                input(USERTASK_SELECTION_TABLES, false).
                input(USERTASK_SELECTION_GROUPING_AND_NESTING, false).
                input(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, true).
                input(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, true));

        completeUsertask(flowExecution, CHECKBOXES_WITH_NESTING, new UsertaskInputBuilder().
                input(CHECKBOXES_WITH_NESTING_CHECKBOX_LEVEL_1_EXPOSES_MORE_DETAIL_CHECKBOX_LEVEL_2_EXPOSES_MORE_DETAIL_TEXT_INPUT_1, TEXT_INPUT));

        completeUsertask(flowExecution, RADIO_BUTTONS_WITH_NESTING, new UsertaskInputBuilder().
                input(RADIO_BUTTONS_WITH_NESTING_RADIO_GROUP_LABEL_RADIO_EXPOSES_MORE_DETAIL_TEXT_INPUT_1, TEXT_INPUT));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 0));

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Interactive_AllSetupUsertasksSelected_ZeroExecuteUsertasks() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = doTestEndToEndFlowExecution_Interactive_SetupPhase_AllUsertasksSelected(executionName);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 0));

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Interactive_AllSetupUsertasksSelected_TwoExecuteUsertasks() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = doTestEndToEndFlowExecution_Interactive_SetupPhase_AllUsertasksSelected(executionName);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 2));

        completeUsertaskNoInput(flowExecution, MULTI_INSTANCE_USERTASK_INSTANCE_0);
        completeUsertaskNoInput(flowExecution, MULTI_INSTANCE_USERTASK_INSTANCE_1);

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    private FlowExecution doTestEndToEndFlowExecution_FileInput_SetupPhase(String executionName) {

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupFile(flowExecution, "setup.zip", getFlowdataFileBytes(flowPackage, "setup.zip"));

        completeUsertaskReviewAndConfirm(flowExecution);

        return flowExecution;
    }

    private FlowExecution doTestEndToEndFlowExecution_Interactive_SetupPhase_AllUsertasksSelected(String executionName) {

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, USERTASK_SELECTION, new UsertaskInputBuilder().
                input(USERTASK_SELECTION_INFORMATION, true).
                input(USERTASK_SELECTION_READ_ONLY_USER_TASK, true).
                input(USERTASK_SELECTION_TEXT_INPUTS, true).
                input(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, true).
                input(USERTASK_SELECTION_DATE_TIME_INPUTS, false).
                input(USERTASK_SELECTION_CHECKBOXES, true).
                input(USERTASK_SELECTION_RADIO_BUTTONS, true).
                input(USERTASK_SELECTION_SELECTBOXES, true).
                input(USERTASK_SELECTION_LISTBOXES, true).
                input(USERTASK_SELECTION_TABLES, true).
                input(USERTASK_SELECTION_GROUPING_AND_NESTING, true).
                input(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, true).
                input(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, true));


        List<UserTask> activeUserTasks = getActiveUsertasksForExecution(flowExecution);
        UserTask informationUserTask = activeUserTasks.get(0);
        checkUserTaskNameExtra(informationUserTask, "NameExtra");

        checkInformationalUsertask(informationUserTask, new UsertaskCheckBuilder()
                .check("Information > This is short information.")
                .check("Information > This is long information. This is long information. This is long information. This is long information. This is long information. This is long information. This is long information. This is long information. ")
                .check("Information > This is a sample link to flowautomation", "https://enmapache.athtem.eei.ericsson.se/#flow-automation")
                .check("Information > This is a sample dynamic link to ENM application", "https://enmapache.athtem.eei.ericsson.se/#shell"));

        completeUsertaskNoInput(flowExecution, "Information");

        List<String> listValues = new ArrayList<>();
        listValues.add("TestValue1");
        listValues.add("TestValue2");

        List<Map<String, Object>> tableList = getSingleSelectTableData();
        List<Map<String, Object>> multiSelectTableList = getMultiSelectTableData();

        completeUsertask(flowExecution, "Read Only User Task", new UsertaskInputBuilder()
                .input("Read Only User Task > Derived Data", "This is the derived information from test. The data for this usertask is generated by the flow.").
                input("Read Only User Task > Short Information","This is short information from test").
                input("Read Only User Task > Derived Date Time","2020-01-27T22:33:44.000Z").
                input("Read Only User Task > Default Date Time","2020-02-27T22:33:44.000Z").
                input("Read Only User Task > Read Only List",listValues).
                input("Read Only User Task > Read Only Table",tableList));

        completeUsertask(flowExecution, "Text Inputs", new UsertaskInputBuilder().
                input(TEXT_INPUTS_TEXT_INPUT_WITH_NO_LENGTH_RESTRICTIONS, TEXT_INPUT_WITH_NO_RESTRICTIONS).
                input("Text Inputs > Short text input", "short").
                input("Text Inputs > Medium text input", "medium length text input").
                input("Text Inputs > Long text input", "long text input long text input long text input long text input").
                input("Text Inputs > Regular expression validated text input", "lowercase text").
                input("Text Inputs > Optional text input", "optional text input").
                input("Text Inputs > Text input with default", "text input with default").
                input("Text Inputs > Text input with dynamic default", "text input with dynamic default"));

        activeUserTasks = getAllUsertasksForExecution(flowExecution);
        informationUserTask = activeUserTasks.get(2);
        checkUserTaskNameExtra(informationUserTask, "NameExtra");

        completeUsertask(flowExecution, "Text Inputs With Information", new UsertaskInputBuilder().
                input("Text Inputs With Information > Text input", TEXT_INPUT));

        completeUsertask(flowExecution, "Check Boxes", new UsertaskInputBuilder().
                input(CHECK_BOXES_SHORT_LABEL, false).
                input(CHECK_BOXES_LONG_LABEL_LONG_LABEL_LONG_LABEL_LONG_LABEL, true));

        completeUsertask(flowExecution, "Radio Buttons", new UsertaskInputBuilder().
                input("Radio Buttons > Radio group label > Short label", true));

        completeUsertask(flowExecution, "Select Boxes", new UsertaskInputBuilder().
                input("Select Boxes > Select box label", SHORT_CONTENTS).
                input("Select Boxes > Dynamic select box label", VALUE_2));

        completeUsertask(flowExecution, "List Boxes", new UsertaskInputBuilder().
                input("List Boxes > List box label", SHORT_CONTENTS).
                input("List Boxes > Dynamic list box label", VALUE_2));

        completeUsertask(flowExecution, "Tables", new UsertaskInputBuilder().
                input("List Tables > Informational Table", Collections.emptyList()).
                input("List Tables > Single Selectable table", tableList).
                input("List Tables > Multi Selectable table", multiSelectTableList));

        completeUsertask(flowExecution, "Grouping And Nesting", new UsertaskInputBuilder().
                input("Grouping And Nesting > Group with text inputs > Text input 1", "text input 1").
                input("Grouping And Nesting > Group with text inputs > Text input 2", "text input 2").
                input("Grouping And Nesting > Group with checkboxes > Checkbox 1", false).
                input("Grouping And Nesting > Group with checkboxes > Checkbox 2", true).
                input("Grouping And Nesting > Group with checkboxes > Checkbox 3", false).
                input("Grouping And Nesting > Group with multiple levels > Level 2 Group > Level 3 Group With Checkboxes > Checkbox 1", false).
                input("Grouping And Nesting > Group with multiple levels > Level 2 Group > Level 3 Group With Checkboxes > Checkbox 2", true));

        completeUsertask(flowExecution, CHECKBOXES_WITH_NESTING, new UsertaskInputBuilder().
                input(CHECKBOXES_WITH_NESTING_CHECKBOX_LEVEL_1_EXPOSES_MORE_DETAIL_CHECKBOX_LEVEL_2_EXPOSES_MORE_DETAIL_TEXT_INPUT_1, TEXT_INPUT));

        completeUsertask(flowExecution, RADIO_BUTTONS_WITH_NESTING, new UsertaskInputBuilder().
                input(RADIO_BUTTONS_WITH_NESTING_RADIO_GROUP_LABEL_RADIO_EXPOSES_MORE_DETAIL_TEXT_INPUT_1, TEXT_INPUT));

        String[] readOnlyListBoxValues = {"Read Only List Value 1","Read Only List Value 2","Read Only List Value 3","Read Only List Value 4"};

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check(USERTASK_SELECTION_INFORMATION, true).
                check(USERTASK_SELECTION_READ_ONLY_USER_TASK, true).
                check(USERTASK_SELECTION_TEXT_INPUTS, true).
                check(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, true).
                check(USERTASK_SELECTION_DATE_TIME_INPUTS, false).
                check(USERTASK_SELECTION_CHECKBOXES, true).
                check(USERTASK_SELECTION_RADIO_BUTTONS, true).
                check(USERTASK_SELECTION_SELECTBOXES, true).
                check(USERTASK_SELECTION_GROUPING_AND_NESTING, true).
                check(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, true).
                check(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, true).
                check("Read Only User Task > Derived Data", "This is the derived information. The data for this usertask is generated by the flow.").
                check("Read Only User Task > Short Information","This is short information").
                check("Read Only User Task > Derived Date Time","2020-02-26T11:22:33.000Z").
                check("Read Only User Task > Default Date Time","2020-03-27T22:33:44.000Z").
                check("Read Only User Task > Read Only List",readOnlyListBoxValues).
                check("Read Only User Task > Read Only Table", getDerivedTableData()).
                check(TEXT_INPUTS_TEXT_INPUT_WITH_NO_LENGTH_RESTRICTIONS, TEXT_INPUT_WITH_NO_RESTRICTIONS).
                check(TEXT_INPUTS_TEXT_INPUT_WITH_NO_LENGTH_RESTRICTIONS, TEXT_INPUT_WITH_NO_RESTRICTIONS).
                check("Text Inputs > Short text input", "short").
                check("Text Inputs > Medium text input", "medium length text input").
                check("Text Inputs > Long text input", "long text input long text input long text input long text input").
                check("Text Inputs > Regular expression validated text input", "lowercase text").
                check("Text Inputs > Optional text input", "optional text input").
                check("Text Inputs > Text input with default", "text input with default").
                check("Text Inputs > Text input with dynamic default", "text input with dynamic default").
                check("Text Inputs With Information > Text input", TEXT_INPUT).
                check(CHECK_BOXES_SHORT_LABEL, false).
                check(CHECK_BOXES_LONG_LABEL_LONG_LABEL_LONG_LABEL_LONG_LABEL, true).
                check("Radio Buttons > Radio group label > Short label", true).
                check("Select Boxes > Select box label", SHORT_CONTENTS).
                check("Select Boxes > Dynamic select box label", VALUE_2).
                check("List Tables > Single Selectable table", tableList).
                check("List Tables > Multi Selectable table", multiSelectTableList).
                check("Grouping And Nesting > Group with text inputs > Text input 1", "text input 1").
                check("Grouping And Nesting > Group with text inputs > Text input 2", "text input 2").
                check("Grouping And Nesting > Group with checkboxes > Checkbox 1", false).
                check("Grouping And Nesting > Group with checkboxes > Checkbox 2", true).
                check("Grouping And Nesting > Group with checkboxes > Checkbox 3", false).
                check("Grouping And Nesting > Group with multiple levels > Level 2 Group > Level 3 Group With Checkboxes > Checkbox 1", false).
                check("Grouping And Nesting > Group with multiple levels > Level 2 Group > Level 3 Group With Checkboxes > Checkbox 2", true).
                check(CHECKBOXES_WITH_NESTING_CHECKBOX_LEVEL_1_EXPOSES_MORE_DETAIL_CHECKBOX_LEVEL_2_EXPOSES_MORE_DETAIL_TEXT_INPUT_1, TEXT_INPUT).
                check(RADIO_BUTTONS_WITH_NESTING_RADIO_GROUP_LABEL_RADIO_EXPOSES_MORE_DETAIL_TEXT_INPUT_1, TEXT_INPUT));

        completeUsertaskReviewAndConfirm(flowExecution);

        return flowExecution;
    }

    private List<Map<String, Object>> getSingleSelectTableData() {

        Map<String,Object> inlineMap2 = new LinkedHashMap<>();
        inlineMap2.put(PRODUCT_ID, 3);
        inlineMap2.put(PRODUCT_NAME, "Product3");
        inlineMap2.put(PACKAGE_NAME, "Package3");
        inlineMap2.put(PACKAGE_TITLE, "Test package title3");
        inlineMap2.put(NODE_DISTANCE, 3);
        inlineMap2.put(NODE_TYPE, "Node Type3");
        inlineMap2.put(CUSTOM_DETAILS, "My custom Details as to be shown for this field3");

        return Stream.of(inlineMap2).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getMultiSelectTableData() {

        Map<String,Object> inlineMap1 = new LinkedHashMap<>();
        inlineMap1.put(PRODUCT_ID, 1);
        inlineMap1.put(PRODUCT_NAME, "Product1");
        inlineMap1.put(PACKAGE_NAME, "Package1");
        inlineMap1.put(PACKAGE_TITLE, "Test package title1");
        inlineMap1.put(NODE_DISTANCE, 1);
        inlineMap1.put(NODE_TYPE, "Node Type1");
        inlineMap1.put(CUSTOM_DETAILS, "My custom Details as to be shown for this field1");

        Map<String,Object> inlineMap2 = new LinkedHashMap<>();
        inlineMap2.put(PRODUCT_ID, 5);
        inlineMap2.put(PRODUCT_NAME, "Product5");
        inlineMap2.put(PACKAGE_NAME, "Package5");
        inlineMap2.put(PACKAGE_TITLE, "Test package title5");
        inlineMap2.put(NODE_DISTANCE, 5);
        inlineMap2.put(NODE_TYPE, "Node Type5");
        inlineMap2.put(CUSTOM_DETAILS, "My custom Details as to be shown for this field5");

        return Stream.of(inlineMap1, inlineMap2).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDerivedTableData() {

        Map<String,Object> inlineMap1 = new LinkedHashMap<>();
        inlineMap1.put(PRODUCT_ID, 0);
        inlineMap1.put(PRODUCT_NAME, "Product0");
        inlineMap1.put(PACKAGE_NAME, "Package0");
        inlineMap1.put(PACKAGE_TITLE, "Test package title0");
        inlineMap1.put(NODE_DISTANCE, 0);
        inlineMap1.put(NODE_TYPE, "Node Type0");
        inlineMap1.put(CUSTOM_DETAILS, "Derived Data generated by the flow0");

        Map<String,Object> inlineMap2 = new LinkedHashMap<>();
        inlineMap2.put(PRODUCT_ID, 1);
        inlineMap2.put(PRODUCT_NAME, "Product1");
        inlineMap2.put(PACKAGE_NAME, "Package1");
        inlineMap2.put(PACKAGE_TITLE, "Test package title1");
        inlineMap2.put(NODE_DISTANCE, 1);
        inlineMap2.put(NODE_TYPE, "Node Type1");
        inlineMap2.put(CUSTOM_DETAILS, "Derived Data generated by the flow1");

        return Stream.of(inlineMap1, inlineMap2).collect(Collectors.toList());
    }

    @Test
    public void testEndToEndFlowExecution_Custom_NestedCheckboxes_Level1OnlySelected() {

        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, USERTASK_SELECTION, new UsertaskInputBuilder().
                input(USERTASK_SELECTION_INFORMATION, false).
                input(USERTASK_SELECTION_READ_ONLY_USER_TASK, false).
                input(USERTASK_SELECTION_TEXT_INPUTS, false).
                input(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, false).
                input(USERTASK_SELECTION_DATE_TIME_INPUTS, false).
                input(USERTASK_SELECTION_CHECKBOXES, false).
                input(USERTASK_SELECTION_RADIO_BUTTONS, false).
                input(USERTASK_SELECTION_SELECTBOXES, false).
                input(USERTASK_SELECTION_LISTBOXES, false).
                input(USERTASK_SELECTION_TABLES, false).
                input(USERTASK_SELECTION_GROUPING_AND_NESTING, false).
                input(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, true).
                input(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, false));

        completeUsertask(flowExecution, CHECKBOXES_WITH_NESTING, new UsertaskInputBuilder().
                input("Checkboxes With Nesting > Checkbox 1 level 1 exposes more detail", true).
                input(CHECKBOXES_WITH_NESTING_CHECKBOX_2_LEVEL_1_EXPOSES_MORE_DETAIL, true));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check("Checkboxes With Nesting > Checkbox 1 level 1 exposes more detail > Checkbox 1 level 2 exposes more detail", false).
                check("Checkboxes With Nesting > Checkbox 2 level 1 exposes more detail > Checkbox 2 level 2 exposes more detail", false));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 0));

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Custom_NestedCheckboxes_OneLevel1OnlySelected() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, USERTASK_SELECTION, new UsertaskInputBuilder().
                input(USERTASK_SELECTION_INFORMATION, false).
                input(USERTASK_SELECTION_READ_ONLY_USER_TASK, false).
                input(USERTASK_SELECTION_TEXT_INPUTS, false).
                input(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, false).
                input(USERTASK_SELECTION_DATE_TIME_INPUTS, false).
                input(USERTASK_SELECTION_CHECKBOXES, false).
                input(USERTASK_SELECTION_RADIO_BUTTONS, false).
                input(USERTASK_SELECTION_SELECTBOXES, false).
                input(USERTASK_SELECTION_LISTBOXES, false).
                input(USERTASK_SELECTION_TABLES, false).
                input(USERTASK_SELECTION_GROUPING_AND_NESTING, false).
                input(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, true).
                input(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, false));

        completeUsertask(flowExecution, CHECKBOXES_WITH_NESTING, new UsertaskInputBuilder().
                input("Checkboxes With Nesting > Checkbox 1 level 1 exposes more detail > Checkbox 1 level 2 exposes more detail > Text input 1", TEXT_INPUT));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check("Checkboxes With Nesting > Checkbox 1 level 1 exposes more detail > Checkbox 1 level 2 exposes more detail> Text input 1", TEXT_INPUT).
                check(CHECKBOXES_WITH_NESTING_CHECKBOX_2_LEVEL_1_EXPOSES_MORE_DETAIL, false));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 0));

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Custom_NestedCheckboxes_NoneSelected() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, USERTASK_SELECTION, new UsertaskInputBuilder().
                input(USERTASK_SELECTION_INFORMATION, false).
                input(USERTASK_SELECTION_READ_ONLY_USER_TASK, false).
                input(USERTASK_SELECTION_TEXT_INPUTS, false).
                input(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, false).
                input(USERTASK_SELECTION_DATE_TIME_INPUTS, false).
                input(USERTASK_SELECTION_CHECKBOXES, false).
                input(USERTASK_SELECTION_RADIO_BUTTONS, false).
                input(USERTASK_SELECTION_SELECTBOXES, false).
                input(USERTASK_SELECTION_LISTBOXES, false).
                input(USERTASK_SELECTION_TABLES, false).
                input(USERTASK_SELECTION_GROUPING_AND_NESTING, false).
                input(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, true).
                input(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, false));

        completeUsertask(flowExecution, CHECKBOXES_WITH_NESTING, new UsertaskInputBuilder().
                input(CHECKBOXES_WITH_NESTING, ""));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check("Checkboxes With Nesting > Checkbox 1 level 1 exposes more detail", false).
                check(CHECKBOXES_WITH_NESTING_CHECKBOX_2_LEVEL_1_EXPOSES_MORE_DETAIL, false));

        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 0));

        checkNumberOfActiveExecutionsForFlow(flowId, 0);
    }

    @Test
    public void testEndToEndFlowExecution_Interactive_MultipleReportTablesPresent() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertask(flowExecution, USERTASK_SELECTION, new UsertaskInputBuilder().
                input(USERTASK_SELECTION_INFORMATION, true).
                input(USERTASK_SELECTION_READ_ONLY_USER_TASK, false).
                input(USERTASK_SELECTION_TEXT_INPUTS, false).
                input(USERTASK_SELECTION_TEXT_INPUTS_WITH_INFORMATION, false).
                input(USERTASK_SELECTION_DATE_TIME_INPUTS, false).
                input(USERTASK_SELECTION_CHECKBOXES, true).
                input(USERTASK_SELECTION_RADIO_BUTTONS, false).
                input(USERTASK_SELECTION_SELECTBOXES, false).
                input(USERTASK_SELECTION_LISTBOXES, false).
                input(USERTASK_SELECTION_TABLES, false).
                input(USERTASK_SELECTION_GROUPING_AND_NESTING, false).
                input(USERTASK_SELECTION_CHECKBOXES_WITH_NESTING, false).
                input(USERTASK_SELECTION_RADIO_BUTTONS_WITH_NESTING, false));

        completeUsertaskNoInput(flowExecution, "Information");

        completeUsertask(flowExecution, "Check Boxes", new UsertaskInputBuilder().
                input(CHECK_BOXES_SHORT_LABEL, false).
                input(CHECK_BOXES_LONG_LABEL_LONG_LABEL_LONG_LABEL_LONG_LABEL, true));


        completeUsertaskReviewAndConfirm(flowExecution);

        completeUsertask(flowExecution, GET_EXECUTE_USERTASK_OPTIONS, new UsertaskInputBuilder().
                input(GET_EXECUTE_USERTASK_OPTIONS_NUMBER_OF_MULTI_INSTANCE_INSTANCES, 4));

        completeUsertaskNoInput(flowExecution, MULTI_INSTANCE_USERTASK_INSTANCE_0);
        completeUsertaskNoInput(flowExecution, MULTI_INSTANCE_USERTASK_INSTANCE_1);
        completeUsertaskNoInput(flowExecution, "Multi-instance Usertask - Instance2");
        completeUsertaskNoInput(flowExecution, "Multi-instance Usertask - Instance3");

        checkExecutionReport(flowExecution);

        checkReportTable(flowExecution, "elementsTable1");
        checkReportTable(flowExecution, "elementsTable2");
    }

}
