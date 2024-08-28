//
//COPYRIGHT Ericsson 2021
//
//
//
//The copyright to the computer program(s) herein is the property of
//
//Ericsson Inc. The programs may be used and/or copied only with written
//
//permission from Ericsson Inc. or in accordance with the terms and
//
//conditions stipulated in the agreement/contract under which the
//
//program(s) have been supplied.
//

import groovy.json.JsonSlurper
import com.ericsson.oss.services.flowautomation.flowapi.EventRecorder
import groovy.json.JsonSlurperClassic

def inputData = execution.getVariableLocal("inputData")
def validationResult = [:]
validationResult.isValid = true

def fileContentsText = inputData.fileInput.value.text
def fileContentsMap = new JsonSlurperClassic().parseText(fileContentsText)

if (!fileContentsMap.containsKey('foo')) {
  def errorMessage = "The file must contain a JSON object with 'foo' key"
  validationResult.isValid = false
  validationResult.message = errorMessage
  EventRecorder.error(execution, errorMessage)
}

execution.setVariableLocal("validationResult", validationResult)
