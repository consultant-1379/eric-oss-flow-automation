import com.ericsson.oss.services.flowautomation.flowapi.EventRecorder

def validationResult = [:]
def errorMessage = "Messages cannot be the same or empty."
validationResult.isValid = true

def stringOne = binding.hasVariable('message1') ? execution.getVariableLocal("message1") : null
def stringTwo = binding.hasVariable('message2') ? execution.getVariableLocal("message2") : null

if (stringOne == null || stringTwo == null || stringOne == stringTwo) {
    validationResult.isValid = false
    validationResult.message = errorMessage
    EventRecorder.error(execution, errorMessage)
}

execution.setVariableLocal("validationResult", validationResult)