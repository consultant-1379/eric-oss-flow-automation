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

import static org.junit.Assert.assertEquals;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

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
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

/**
 * Usertask showcase flow tests
 */
@SuppressWarnings({"squid:S00100"})
public abstract class DslSdkUserTaskWidgetsFlowExampleTest extends FlowAutomationBaseTest {

    public static final String NODE_2 = "node2";
    public static final String LTE_01_DG_2_ERBS_00001 = "LTE01dg2ERBS00001";
    public static final String LTE_01_DG_2_ERBS_00002 = "LTE01dg2ERBS00002";
    public static final String LTE_01_DG_2_ERBS_00003 = "LTE01dg2ERBS00003";
    public static final String LTE_01_DG_2_ERBS_00004 = "LTE01dg2ERBS00004";
    String flowPackage = "sdkUserTaskWidgetsFlowExample";
    String flowId = "com.ericsson.oss.fa.flows.sdkUserTaskWidgetsFlowExample";
    FlowDefinition flowDefinition;

    private static final String SIMULATION_RESULTS = "Simulation Results";
    private static final String LTE_NODES = "LTE01dg2ERBS00001,LTE01dg2ERBS00002,LTE01dg2ERBS00003,LTE01dg2ERBS00004";
    private static final String NUMBER_OF_SIMULATIONS = "numberOfSimulations";
    private static final String NODE_1 = "node1";

    @Before
    public void before() {
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @After
    public void after() {
        removeFlow(flowId);
    }

    @Test
    public void testEndToEndFlowExecution_Interactive() {
        String executionName = createUniqueInstanceName(flowId);

        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        completeUsertaskChooseSetupInteractive(flowExecution);

        completeUsertaskNoInput(flowExecution, "License Check");

        completeUsertask(flowExecution, "Select Nodes", new UsertaskInputBuilder().
                input("Select Nodes > Selected Nodes count", LTE_NODES).
                input("Select Nodes > Selected Collections count", "").
                input("Select Nodes > Selected Savedsearch count", ""));

        List<Map<String, Object>> relationTableList = getRelationsTableData();

        completeUsertask(flowExecution, "Select Relations", new UsertaskInputBuilder().
                input("Select Relations > Selected Relations", relationTableList));

        completeUsertask(flowExecution, "Additional Settings", new UsertaskInputBuilder().
                input("Additional Settings > Simulation Name", "name1").
                input("Additional Settings > Simulation Date/Time", "2020-03-27T22:33:44.000Z").
                input("Additional Settings > Intensity of Scanning", "Medium").
                input("Additional Settings > Speed of Scanning", "Slow").
                input("Additional Settings > More than one iteration in scanning", true).
                input("Additional Settings > More than one iteration in scanning > Number of Iterations", 4));

        checkUsertaskReviewAndConfirm(flowExecution, new UsertaskCheckBuilder().
                check("Select Nodes > Selected Nodes count", LTE_NODES).
                check("Select Nodes > Selected Collections count", "").
                check("Select Nodes > Selected Savedsearch count", "").
                check("Select Relations > Selected Relations", relationTableList).
                check("Additional Settings > Simulation Name", "name1").
                check("Additional Settings > Simulation Date/Time", "2020-03-27T22:33:44.000Z").
                check("Additional Settings > Intensity of Scanning", "Medium").
                check("Additional Settings > Speed of Scanning", "Slow").
                check("Additional Settings > More than one iteration in scanning", true).
                check("Additional Settings > More than one iteration in scanning > Number of Iterations", 4));

        completeUsertaskReviewAndConfirm(flowExecution);

        checkUsertaskActive(flowExecution, SIMULATION_RESULTS);

        checkExecutionReport(flowExecution);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "nodesSelected", LTE_NODES);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, NUMBER_OF_SIMULATIONS, "1");
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "LTE01dg2ERBS00001:LTE01dg2ERBS00002.node1", LTE_01_DG_2_ERBS_00001);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "LTE01dg2ERBS00001:LTE01dg2ERBS00002.node2", LTE_01_DG_2_ERBS_00002);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "LTE01dg2ERBS00001:LTE01dg2ERBS00003.node1", LTE_01_DG_2_ERBS_00001);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "LTE01dg2ERBS00001:LTE01dg2ERBS00003.node2", LTE_01_DG_2_ERBS_00003);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "LTE01dg2ERBS00002:LTE01dg2ERBS00004.node1", LTE_01_DG_2_ERBS_00002);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, "LTE01dg2ERBS00002:LTE01dg2ERBS00004.node2", LTE_01_DG_2_ERBS_00004);

        completeUsertask(flowExecution, SIMULATION_RESULTS, new UsertaskInputBuilder().
                input("Simulation Results > Do you want to restart the simulation? > Yes", true));

        checkNumberOfActiveExecutionsForFlow(flowId, 1);

        checkUsertaskActive(flowExecution, SIMULATION_RESULTS);

        checkExecutionReport(flowExecution);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, NUMBER_OF_SIMULATIONS, "2");

        completeUsertask(flowExecution, SIMULATION_RESULTS, new UsertaskInputBuilder().
                input("Simulation Results > Do you want to restart the simulation? > No", true));

        checkExecutionReport(flowExecution);
        downloadAndCheckReportVariableContentEqualsTo(flowExecution, NUMBER_OF_SIMULATIONS, "2");

        checkNumberOfActiveExecutionsForFlow(flowId, 0);

        FlowExecutionResource allResources = downloadFlowExecutionReport(flowExecution);
        assertEquals(flowExecution.getName()+"-report.xlsx", allResources.getName());
    }

    private List<Map<String, Object>> getRelationsTableData() {

        Map<String,Object> inlineMap1 = new LinkedHashMap<>();
        inlineMap1.put(NODE_1, LTE_01_DG_2_ERBS_00001);
        inlineMap1.put(NODE_2, LTE_01_DG_2_ERBS_00002);

        Map<String,Object> inlineMap2 = new LinkedHashMap<>();
        inlineMap2.put(NODE_1, LTE_01_DG_2_ERBS_00001);
        inlineMap2.put(NODE_2, LTE_01_DG_2_ERBS_00003);

        Map<String,Object> inlineMap3 = new LinkedHashMap<>();
        inlineMap3.put(NODE_1, LTE_01_DG_2_ERBS_00002);
        inlineMap3.put(NODE_2, LTE_01_DG_2_ERBS_00004);

        return Stream.of(inlineMap1, inlineMap2, inlineMap3).collect(Collectors.toList());
    }
}
