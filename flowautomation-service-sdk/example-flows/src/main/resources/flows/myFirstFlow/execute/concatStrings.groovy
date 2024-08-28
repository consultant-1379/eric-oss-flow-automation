package execute

import com.ericsson.oss.services.flowautomation.flowapi.EventRecorder
import org.camunda.bpm.engine.delegate.BpmnError

def stringOne = binding.hasVariable('string1') ? execution.getVariableLocal("string1") : null
def stringTwo = binding.hasVariable('string2') ? execution.getVariableLocal("string2") : null

if (stringOne == null || stringTwo == null) {
    EventRecorder.error(execution, "Strings cannot be the same or empty.")
    throw new BpmnError("InputValidationError")
}

def concatenatedString = stringOne + " " + stringTwo
EventRecorder.info(execution, "Concatenated " + stringOne + " and " + stringTwo)
execution.setVariableLocal("concatenatedString", concatenatedString)