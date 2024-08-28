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

package com.ericsson.oss.services.flowautomation.flow.listeners;

import static com.ericsson.oss.services.flowautomation.deployment.processor.FlowAutomationDeploymentProcessorImpl.FA_BACK_TARGET;

import java.util.Collection;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

/**
 * The listener for the usertasks that have the faBackTarget extension configured.
 */
public class UserTaskExecutionStartListener implements ExecutionListener {

    public static final String BACK_TARGET = "FaInternal_BACK_TARGET";

    /**
     * Sets a execution local variable {@value #BACK_TARGET} to value configured in faBackTarget extension.
     *
     * @param delegateExecution the execution
     * @throws Exception when error happens
     */
    @Override
    public void notify(final DelegateExecution delegateExecution) throws Exception {
        final ExtensionElements extensionElements = delegateExecution.getBpmnModelElementInstance().getExtensionElements();
        final Collection<CamundaProperty> camundaProperties = extensionElements.getElementsQuery().filterByType(CamundaProperties.class).singleResult().getCamundaProperties();
        final CamundaProperty backTargetProperty = camundaProperties.stream()
                .filter(property -> Objects.equals(FA_BACK_TARGET, property.getCamundaName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("The faBackTarget property is missing. this listener shouldn't have been called"));
        delegateExecution.setVariableLocal(BACK_TARGET, backTargetProperty.getCamundaValue());
    }
}
