package com.ericsson.oss.services.flowautomation.flows.test.myFirstFlow

import static org.junit.Assert.assertEquals

import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationScriptBaseTest
import org.camunda.bpm.engine.delegate.BpmnError
import org.junit.Test

class ConcatMessagesScriptTest extends FlowAutomationScriptBaseTest{
    @Test
    void testScript() {
        def message1 = "Hello"
        def message2 = "World"

        delegateExecution.setVariableLocal("string1", message1)
        delegateExecution.setVariableLocal("string2", message2)

        runFlowScript(delegateExecution, "flows/myFirstFlow/execute/concatStrings.groovy")
        assertEquals(message1 + " " + message2, delegateExecution.getVariableLocal("concatenatedString"))
        checkExecutionEventIsRecorded(flowExecution, "INFO", "Concatenated " + message1 + " and " + message2)
    }

    @Test(expected = BpmnError.class)
    void testScriptBpmnError_NoInput() {
        // Expect error if input parameter missing
        runFlowScript(delegateExecution, "flows/myFirstFlow/execute/concatStrings.groovy")
    }
}
