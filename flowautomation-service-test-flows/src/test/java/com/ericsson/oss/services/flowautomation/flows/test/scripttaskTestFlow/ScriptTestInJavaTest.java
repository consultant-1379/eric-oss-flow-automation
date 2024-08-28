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

package com.ericsson.oss.services.flowautomation.flows.test.scripttaskTestFlow;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationScriptBaseTest;

public class ScriptTestInJavaTest extends FlowAutomationScriptBaseTest {

    @Test
    public void testScriptChangeVariable() {
        delegateExecution.setVariable("myvar", "value1");
        runFlowScript(delegateExecution, "flows/scripttaskTestFlow/execute/changeVariable.groovy");
        assertEquals("value2", delegateExecution.getVariable("myvar"));
    }

    @Test(expected = BpmnError.class)
    public void testScriptBpmnError() {
        delegateExecution.setVariable("doThrowError", "true");
        runFlowScript(delegateExecution, "flows/scripttaskTestFlow/execute/throwError.groovy");
    }

    @Test
    public void testScriptUpdateSummaryReport() {
        runFlowScript(delegateExecution, "flows/scripttaskTestFlow/execute/updateSummaryReport.groovy");
        checkExecutionSummaryReport(flowExecution, "Reporting stuff");
    }

    @Test
    public void testScriptLogEvent() {
        runFlowScript(delegateExecution, "flows/scripttaskTestFlow/execute/logEvent.groovy");
        checkExecutionEventIsRecorded(flowExecution, "INFO", "Executing:"+flowExecution.getFlowId()+"-"+flowExecution.getName());
    }
}
