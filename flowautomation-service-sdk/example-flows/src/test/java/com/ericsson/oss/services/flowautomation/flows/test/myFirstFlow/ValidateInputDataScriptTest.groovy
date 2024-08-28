package com.ericsson.oss.services.flowautomation.flows.test.myFirstFlow

import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationScriptBaseTest
import org.camunda.bpm.engine.delegate.BpmnError
import org.junit.Test

import static org.junit.Assert.assertEquals

class ValidateInputDataScriptTest extends FlowAutomationScriptBaseTest {
    @Test
    void testScript() {
        def message1 = "Hello"
        def message2 = "World"

        delegateExecution.setVariableLocal("message1", message1)
        delegateExecution.setVariableLocal("message2", message2)

        runFlowScript(delegateExecution, "flows/myFirstFlow/setup/validateInputData.groovy")
        assertEquals(true, delegateExecution.getVariableLocal("validationResult").isValid)
    }

    @Test
    void testScriptValidationFailed_NoInput() {
        runFlowScript(delegateExecution, "flows/myFirstFlow/setup/validateInputData.groovy")
        assertEquals(false, delegateExecution.getVariableLocal("validationResult").isValid)
    }

    @Test
    void testScriptBpmnError_SameInput() {
        def message1 = "Hello"
        def message2 = "Hello"

        delegateExecution.setVariableLocal("message1", message1)
        delegateExecution.setVariableLocal("message2", message2)

        runFlowScript(delegateExecution, "flows/myFirstFlow/setup/validateInputData.groovy")
        assertEquals(false, delegateExecution.getVariableLocal("validationResult").isValid)
    }
}
