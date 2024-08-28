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

import static com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationServiceUtil.VARIABLE_EXPRESSION_DELIMITER_SPLIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.MultiInstanceLoopCharacteristicsImpl;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import com.ericsson.oss.services.flowautomation.deployment.processor.FlowAutomationDeploymentProcessorImpl;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationServiceUtil;

/*
 * MI start listener
 */
public class MultiInstanceStartListener implements ExecutionListener {

    private static final String FA_INTERNAL_VARIABLE_ACTIVITY_INSTANCE_NAME = "faActivityInstanceName";

    private static final String INSTANCE_VARIABLE_DELIMITER = "/";

    @Override
    public void notify(final DelegateExecution execution) throws Exception {
        // Look for "faActivityInstanceVariableName" property and, if present, use it to store local "faActivityInstanceName" variable.
        // If property not present no instance name will be set for this MI activity.

        final BpmnModelInstance modelInstance = execution.getBpmnModelInstance();
        final ModelElementInstance modelElementInstance = execution.getBpmnModelElementInstance();
        final Collection<ModelElementInstance> extensionsCollection = modelElementInstance.getChildElementsByType(modelInstance.getModel().getType(ExtensionElements.class));
        if (!extensionsCollection.isEmpty()) {
            final ModelElementInstance extensions = extensionsCollection.iterator().next();
            final Collection<ModelElementInstance> properties = extensions.getChildElementsByType(modelInstance.getModel().getType(CamundaProperties.class));
            if (!properties.isEmpty()) {
                final CamundaProperties xproperties = (CamundaProperties)properties.iterator().next();
                processProperties(execution, xproperties.getCamundaProperties());
            }
        }
    }

    private void processProperties(final DelegateExecution execution, final Collection<CamundaProperty> properties) {
        for (CamundaProperty property : properties) {
            if (property.getCamundaName().equals(FlowAutomationDeploymentProcessorImpl.FA_ACTIVITY_INSTANCE_VARIABLE_NAME)) {
                final String faActivityInstanceVariableName = property.getCamundaValue();
                boolean isSequential = false;
                final Collection<Activity> activities = execution.getBpmnModelInstance().getModelElementsByType(Activity.class);
                if (!(activities.isEmpty())) {
                    for (Activity activity : activities) {
                        Collection<ModelElementInstance> miLoopCharacteristicsElement =
                                activity.getChildElementsByType(execution.getBpmnModelInstance().getModel().getType(MultiInstanceLoopCharacteristics.class));
                        if (miLoopCharacteristicsElement != null && !miLoopCharacteristicsElement.isEmpty()) {
                            if (((MultiInstanceLoopCharacteristicsImpl) ((ArrayList) miLoopCharacteristicsElement).get(0)).isSequential()) {
                                isSequential = true;
                            }
                        }
                    }
                }
                if (faActivityInstanceVariableName.indexOf(FlowAutomationServiceUtil.VARIABLE_EXPRESSION_DELIMITER) == -1) {
                    handleNonExpressionVariable(execution, faActivityInstanceVariableName, isSequential);
                }
                else {
                    handleExpressionVariable(execution, faActivityInstanceVariableName, isSequential);
                }
                break;
            }
        }
    }

    private void handleNonExpressionVariable(DelegateExecution execution, final String faActivityInstanceVariableName, final boolean isSequential) {
        Object faActivityInstanceVariable = execution.getVariable(faActivityInstanceVariableName);
        String faActivityInstanceVariableKey = FA_INTERNAL_VARIABLE_ACTIVITY_INSTANCE_NAME;

        if (faActivityInstanceVariable instanceof String) {
            if (isSequential) {
                faActivityInstanceVariableKey = faActivityInstanceVariableKey.concat(INSTANCE_VARIABLE_DELIMITER + execution.getActivityInstanceId());
            }
            final String faActivityInstanceVariableValue = execution.getActivityInstanceId() + INSTANCE_VARIABLE_DELIMITER + execution.getVariable(faActivityInstanceVariableName);
            execution.setVariableLocal(faActivityInstanceVariableKey, faActivityInstanceVariableValue);
        }
    }

    private void handleExpressionVariable(DelegateExecution execution, final String faActivityInstanceVariableName, final boolean isSequential) {
        // The expression should consist of <mapVariable>.<instanceVariableName> - would be created by FlowAutomationDeploymentProcessorImpl
        // If the expression does not match this pattern then ignore it
        String faActivityInstanceVariableKey = FA_INTERNAL_VARIABLE_ACTIVITY_INSTANCE_NAME;
        final String[] bits = faActivityInstanceVariableName.split(VARIABLE_EXPRESSION_DELIMITER_SPLIT);
        if (bits.length == 2) {
            final String mapVariableName = bits[0];
            final String instanceVariableName = bits[1];
            final Object mapVariableObject = execution.getVariable(mapVariableName);
            if (mapVariableObject instanceof Map) {
                final Map<String, Object> mapVariable = (Map<String, Object>)mapVariableObject;
                final Object faActivityInstanceVariableValueObject = mapVariable.get(instanceVariableName);
                if (faActivityInstanceVariableValueObject instanceof String) {
                    if (isSequential) {
                        faActivityInstanceVariableKey = faActivityInstanceVariableKey.concat(INSTANCE_VARIABLE_DELIMITER + execution.getActivityInstanceId());
                    }
                    String faActivityInstanceVariableValue = execution.getActivityInstanceId() + INSTANCE_VARIABLE_DELIMITER + faActivityInstanceVariableValueObject;
                    execution.setVariableLocal(faActivityInstanceVariableKey, faActivityInstanceVariableValue);
                }
            }
        }
    }
}
