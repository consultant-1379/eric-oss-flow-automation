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
package flows.scripttaskTestFlow.execute

import org.camunda.bpm.engine.delegate.BpmnError

if (doThrowError == "true") {
    throw new org.camunda.bpm.engine.delegate.BpmnError("some-error")
}
