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

package com.ericsson.oss.services.flowautomation.flowapi;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * The Class AddMultiInstanceElement.
 */
public class AddMultiInstanceElement implements JavaDelegate {

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        final String elementName = (String) execution.getVariableLocal("elementName");
        final Object elementValue = execution.getVariableLocal("elementValue");
        final String multiInstanceActivityName = (String) execution.getVariableLocal("multiInstanceActivityName");

        execution.getProcessEngineServices().getRuntimeService().createProcessInstanceModification(execution.getProcessInstanceId())
                .startBeforeActivity(multiInstanceActivityName).setVariableLocal(elementName, elementValue).execute();
    }
}
