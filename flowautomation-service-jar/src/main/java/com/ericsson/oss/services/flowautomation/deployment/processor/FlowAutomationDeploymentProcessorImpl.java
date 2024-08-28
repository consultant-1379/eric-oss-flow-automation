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

package com.ericsson.oss.services.flowautomation.deployment.processor;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.MISSING_BUSINESS_KEY_PROPAGATION_ON_CALL_ACTIVITY;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.MISSING_BUSINESS_KEY_PROPAGATION;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationServiceUtil.CAMUNDA_ID_DELIMITER;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationServiceUtil.VARIABLE_EXPRESSION_DELIMITER;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionWrapperGenerator.FLOW_ID;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionWrapperGenerator.VERSION;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.REVIEW_AND_CONFIRM_EXECUTE_USER_TASK;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer.BPMN_RESOURCE_SUFFIXES;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Error;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import com.ericsson.oss.services.flowautomation.exception.ValidationException;
import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionWrapperGenerator;
import com.ericsson.oss.services.flowautomation.flow.utils.SchemaUtils;

import javax.inject.Inject;

/**
 * The Class FlowAutomationDeploymentProcessorImpl.
 */
public class FlowAutomationDeploymentProcessorImpl implements FlowAutomationDeploymentProcessor {

    private static final String DOLLAR_SIGN = "$";

    private static final String BUSINESS_KEY = "businessKey";

    private static final String IN = "in";

    private static final String FA_INTERNAL_FLOW_ID_PREFIX = "com.ericsson.oss.fa.internal";

    private static final String FA_INCIDENT_HANDLER_START_EVENT_ID = "fa-incident-handler-start-event";
    private static final String FA_INCIDENT_HANDLER_ERROR_START_EVENT_ID = "fa-incident-handler-error-start-event";
    private static final String FA_INCIDENT_HANDLER_ERROR_START_EVENT_NAME = "fa.error.incident.handle";

    private static final String FA_INCIDENT_HANDLER_END_EVENT_ID = "fa-incident-handler-end-event";
    private static final String FA_INCIDENT_HANDLER_ERROR_END_EVENT_ID = "fa-incident-handler-error-end-event";
    private static final String FA_INCIDENT_HANDLER_ERROR_END_EVENT_NAME = "fa.error.incident.throw";

    private static final String FA_INCIDENT_HANDLER_EVENT_SUB_PROCESS_ID = "fa-incident-handler";
    private static final String FA_INCIDENT_HANDLER_EVENT_SUB_PROCESS_NAME = "FA incident handler";

    private static final String FA_INCIDENT_HANDLER_ERROR_START_EVENT_DEFINITION_ID = "fa-incident-handler-error-start-event-definition";
    private static final String FA_INCIDENT_HANDLER_ERROR_END_EVENT_DEFINITION_ID = "fa-incident-handler-error-end-event-definition";

    private static final String FA_INCIDENT_HANDLER_ERROR_SEQUENCE_FLOW_ID = "fa-incident-handler-sequence-flow";
    public static final String FA_ACTIVITY_INSTANCE_VARIABLE_NAME = "faActivityInstanceVariableName";
    private static final String CAMUNDA_BPMN_MODEL_NAMESPACE = "http://camunda.org/schema/1.0/bpmn";
    private static final String CAMUNDA_BPMN_MODEL_MULTI_INSTANCE_ELEMENT_VARIABLE = "elementVariable";
    private static final String MULTI_INSTANCE_START_LISTENER_CLASS = "com.ericsson.oss.services.flowautomation.flow.listeners.MultiInstanceStartListener";
    public static final String FA_BACK_TARGET = "faBackTarget";

    @Inject
    private JsonSchemaValidator jsonSchemaValidator;

    @Override
    public void processDeployment(final String processDefinitionKey, final DeploymentBuilder deploymentBuilder) {
        final DeploymentBuilderImpl deploymentBuilderImpl = (DeploymentBuilderImpl) deploymentBuilder;
        final DeploymentEntity deployment = deploymentBuilderImpl.getDeployment();
        final Map<String, ResourceEntity> deployingResources = deployment.getResources();
        final List<String> bpmnResources = getBpmnResources(deployingResources);
        final List<String> nonBpmnResources = getNonBpmnResources(deployingResources);
        bpmnResources.forEach(resourceName -> {
            final ResourceEntity resource = deployment.getResource(resourceName);
            modifyBpmn(processDefinitionKey, resource);
        });

        nonBpmnResources.forEach(resourceName -> {
            final ResourceEntity resource = deployment.getResource(resourceName);
            modifyNonBpmn(resource);
            Map<String, Object> schemaMap = getResourceSchemaMap(resource);
            if (schemaMap != null) {
                jsonSchemaValidator.validate(schemaMap, resource.getName());
            }
        });
    }

    /**
     * coverts the non-bpmn resource to a Map object.
     *
     * @param resource the non bpmn resource
     * @return
     */
    private Map<String, Object> getResourceSchemaMap(final ResourceEntity resource) {
        final String utf8 = new String(resource.getBytes(), StandardCharsets.UTF_8);
        if (FilenameUtils.getExtension(resource.getName()).equalsIgnoreCase("json") && !utf8.isEmpty()) {
            return SchemaUtils.getSchemaMap(utf8);
        }
        return null;
    }

    @Override
    public String generateProcessId(final String processDefinitionKey, final String processId) {
        if (!isProcessIdPrefixed(processDefinitionKey, processId)) {
            return processDefinitionKey.concat(CAMUNDA_ID_DELIMITER).concat(processId);
        }

        return processId;
    }

    /**
     * Gets the bpmn resources.
     *
     * @param resources the resources
     * @return the bpmn resources
     */
    private List<String> getBpmnResources(final Map<String, ResourceEntity> resources) {
        return resources.keySet().stream().filter(this::isBpmnResource).collect(Collectors.toList());
    }

    private List<String> getNonBpmnResources(final Map<String, ResourceEntity> resources) {
        return resources.keySet().stream().filter(this::isNonBpmnResource).collect(Collectors.toList());
    }

    /**
     * Checks if resource is a BPMN resource.
     *
     * @param resourceName the resource name
     * @return true, if is bpmn resource
     */
    private boolean isBpmnResource(final String resourceName) {
        for (final String suffix : BPMN_RESOURCE_SUFFIXES) {
            if (resourceName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNonBpmnResource(final String resourceName) {
        return !resourceName.endsWith("bpmn");
    }

    /**
     * Modify bpmn.
     *
     * @param processDefinitionKey the process definition key
     * @param resource             the resource
     */
    private void modifyBpmn(final String processDefinitionKey, final ResourceEntity resource) {
        final BpmnModelInstance modelInstance = readResourceModelInstance(resource);

        // Modify process ID
        final Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
        processes.forEach(process -> {
            final String processId = process.getId();
            final String uniqueProcessId = generateProcessId(processDefinitionKey, processId);
            process.setId(uniqueProcessId);
        });

        // Check call activities - must propagate business key, modify called process ID
        final Collection<CallActivity> callActivities = modelInstance.getModelElementsByType(CallActivity.class);
        if (!(callActivities.isEmpty())) {
            callActivities.forEach(callActivity -> {
                final ExtensionElements extensionElements = callActivity.getExtensionElements();
                if (!isBusinessKeyPropagated(extensionElements)) {
                    final Map<String, String> flowIdFlowVersion = ExecutionWrapperGenerator.getFlowIdFlowVersion(processDefinitionKey);
                    throw new ValidationException(MISSING_BUSINESS_KEY_PROPAGATION, format(MISSING_BUSINESS_KEY_PROPAGATION_ON_CALL_ACTIVITY,
                            flowIdFlowVersion.get(FLOW_ID), flowIdFlowVersion.get(VERSION), callActivity.getId(), callActivity.getName()));
                }

                final String calledProcessId = callActivity.getCalledElement();
                if (!calledProcessId.startsWith(DOLLAR_SIGN)) {
                    callActivity.setCalledElement(generateProcessId(processDefinitionKey, calledProcessId));
                }
            });
        }

        // Only add incident handler to non-internal flows
        if (!processDefinitionKey.startsWith(FA_INTERNAL_FLOW_ID_PREFIX)) {
            addIncidentHandler(modelInstance);
        }

        // Add properties and execution listener for multi-instance for instance name identification for GUI
        final Collection<Activity> activities = modelInstance.getModelElementsByType(Activity.class);
        if (!(activities.isEmpty())) {
            activities.forEach(activity -> {
                final Collection<ModelElementInstance> miLoopCharacteristicsElement =
                        activity.getChildElementsByType(modelInstance.getModel().getType(MultiInstanceLoopCharacteristics.class));
                for (final ModelElementInstance miLoopCharacteristics : miLoopCharacteristicsElement) {
                    processMultiInstance(modelInstance, activity, miLoopCharacteristics);
                }
            });
        }

        addExecutionListenersForUsertaksWithBackTarget(modelInstance);
        addExecutionListenerForReviewAndConfirmUserTask(modelInstance);
        writeBpmnForDeployment(resource, modelInstance);
    }

    private void addExecutionListenerForReviewAndConfirmUserTask(final BpmnModelInstance modelInstance) {
        modelInstance.getModelElementsByType(UserTask.class).stream()
                .filter(userTask -> Objects.equals(REVIEW_AND_CONFIRM_EXECUTE_USER_TASK.getId(), userTask.getId()))
                .forEach(userTask -> addExecutionStartListener(modelInstance, userTask, "com.ericsson.oss.services.flowautomation.flow.wrapper.ReviewAndConfirmUserTaskStartListener"));
    }


    private void modifyNonBpmn(final ResourceEntity resource) {
        final byte[] bytes = resource.getBytes();
        if (bytes != null) {
            final String utf8 = new String(bytes, StandardCharsets.UTF_8);
            resource.setBytes(utf8.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void addExecutionStartListener(final BpmnModelInstance modelInstance, final Task task, final String listener) {
        ExtensionElements extensionElements = task.getExtensionElements();

        if (extensionElements == null) {
            extensionElements = createElement(modelInstance, ExtensionElements.class);
            task.addChildElement(extensionElements);
        }

        final CamundaExecutionListener startListener = createElement(modelInstance, CamundaExecutionListener.class);
        startListener.setCamundaClass(listener);
        startListener.setCamundaEvent(ExecutionListener.EVENTNAME_START);
        extensionElements.addChildElement(startListener);
    }

    @SuppressWarnings({"squid:S1643"})
    private void processMultiInstance(final BpmnModelInstance modelInstance, final Activity activity, final ModelElementInstance miLoopCharacteristics) {
        String elementVariableName = miLoopCharacteristics.getAttributeValueNs(CAMUNDA_BPMN_MODEL_NAMESPACE, CAMUNDA_BPMN_MODEL_MULTI_INSTANCE_ELEMENT_VARIABLE);

        ExtensionElements extensionElements = activity.getExtensionElements();
        if (extensionElements == null) {
            extensionElements = createElement(modelInstance, ExtensionElements.class);
            activity.addChildElement(extensionElements);
        }

        boolean faActivityInstanceVariableNamePropertyDefined = false;
        final Collection<ModelElementInstance> properties = extensionElements.getChildElementsByType(modelInstance.getModel().getType(CamundaProperties.class));
        if (!properties.isEmpty()) {
            final CamundaProperties xproperties = (CamundaProperties)properties.iterator().next();
            final Collection<CamundaProperty> yproperties = xproperties.getCamundaProperties();
            for (final CamundaProperty property : yproperties) {
                if (property.getCamundaName().equals(FA_ACTIVITY_INSTANCE_VARIABLE_NAME)) {
                    faActivityInstanceVariableNamePropertyDefined = true;
                    elementVariableName = elementVariableName + VARIABLE_EXPRESSION_DELIMITER + property.getCamundaValue();
                    property.setCamundaValue(elementVariableName);
                    break;
                }
            }
        }

        if (!faActivityInstanceVariableNamePropertyDefined) {
            final CamundaProperties xproperties = createElement(modelInstance, CamundaProperties.class);
            final CamundaProperty property = createElement(modelInstance, CamundaProperty.class);
            property.setCamundaName(FA_ACTIVITY_INSTANCE_VARIABLE_NAME);
            property.setCamundaValue(elementVariableName);
            xproperties.addChildElement(property);
            extensionElements.addChildElement(xproperties);
        }

        final CamundaExecutionListener startListener = createElement(modelInstance, CamundaExecutionListener.class);
        startListener.setCamundaClass(MULTI_INSTANCE_START_LISTENER_CLASS);
        startListener.setCamundaEvent(ExecutionListener.EVENTNAME_START);

        extensionElements.addChildElement(startListener);
    }

    private void addIncidentHandler(final BpmnModelInstance modelInstance) {
        final ModelElementType definitionsType = modelInstance.getModel().getType(Definitions.class);
        final Collection<ModelElementInstance> definitionsInstances = modelInstance.getModelElementsByType(definitionsType);
        final ModelElementInstance definitionsInstance = definitionsInstances.iterator().next();

        final Error incidentErrorInternal = createErrorElement(modelInstance, FA_INCIDENT_HANDLER_ERROR_START_EVENT_ID, FA_INCIDENT_HANDLER_ERROR_START_EVENT_NAME, FA_INCIDENT_HANDLER_ERROR_START_EVENT_NAME);
        definitionsInstance.addChildElement(incidentErrorInternal);

        final Error incidentError = createErrorElement(modelInstance, FA_INCIDENT_HANDLER_ERROR_END_EVENT_ID, FA_INCIDENT_HANDLER_ERROR_END_EVENT_NAME, FA_INCIDENT_HANDLER_ERROR_END_EVENT_NAME);
        definitionsInstance.addChildElement(incidentError);

        final ModelElementType processType = modelInstance.getModel().getType(Process.class);
        final Collection<ModelElementInstance> processInstances = modelInstance.getModelElementsByType(processType);
        final ModelElementInstance processInstance = processInstances.iterator().next();

        final SubProcess subprocess = createEventSubProcess(modelInstance, FA_INCIDENT_HANDLER_EVENT_SUB_PROCESS_ID, FA_INCIDENT_HANDLER_EVENT_SUB_PROCESS_NAME);
        processInstance.addChildElement(subprocess);

        final StartEvent startEvent = createElement(modelInstance, FA_INCIDENT_HANDLER_START_EVENT_ID, StartEvent.class);
        startEvent.setCamundaAsyncBefore(true);
        final ErrorEventDefinition startErrorEventDefinition = createElement(modelInstance, FA_INCIDENT_HANDLER_ERROR_START_EVENT_DEFINITION_ID, ErrorEventDefinition.class);
        startErrorEventDefinition.setError(incidentErrorInternal);
        startEvent.addChildElement(startErrorEventDefinition);

        final EndEvent endEvent = createElement(modelInstance, FA_INCIDENT_HANDLER_END_EVENT_ID, EndEvent.class);
        endEvent.setCamundaAsyncAfter(true);
        final ErrorEventDefinition endErrorEventDefinition = createElement(modelInstance, FA_INCIDENT_HANDLER_ERROR_END_EVENT_DEFINITION_ID, ErrorEventDefinition.class);
        endErrorEventDefinition.setError(incidentError);
        endEvent.addChildElement(endErrorEventDefinition);

        subprocess.addChildElement(startEvent);
        subprocess.addChildElement(endEvent);

        final SequenceFlow sequenceFlow = createElement(modelInstance, FA_INCIDENT_HANDLER_ERROR_SEQUENCE_FLOW_ID, SequenceFlow.class);
        subprocess.addChildElement(sequenceFlow);
        sequenceFlow.setSource(startEvent);
        startEvent.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(endEvent);
        endEvent.getIncoming().add(sequenceFlow);
    }

    private SubProcess createEventSubProcess(final BpmnModelInstance modelInstance, final String id, final String name) {
        final SubProcess subprocess = createElement(modelInstance, id, SubProcess.class);
        subprocess.setName(name);
        subprocess.setTriggeredByEvent(true);
        return subprocess;
    }

    private Error createErrorElement(final BpmnModelInstance modelInstance, final String id, final String name, final String errorName) {
        final Error error = createElement(modelInstance, id, Error.class);
        error.setAttributeValue("id", id, true);
        error.setName(name);
        error.setErrorCode(errorName);
        return error;
    }

    private <T extends BpmnModelElementInstance> T createElement(final BpmnModelInstance modelInstance, final String id, final Class<T> elementClass) {
        final T element = modelInstance.newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        return element;
    }

    private <T extends BpmnModelElementInstance> T createElement(final BpmnModelInstance modelInstance, final Class<T> elementClass) {
        return modelInstance.newInstance(elementClass);
    }

    /**
     * Checks if business key is propagated.
     *
     * @param extensionElements the extension elements
     * @return true, if is business key propagated
     */
    private boolean isBusinessKeyPropagated(final ExtensionElements extensionElements) {
        if (extensionElements == null) {
            return false;
        }

        final Collection<ModelElementInstance> modelElementInstances = extensionElements.getElements();
        return modelElementInstances.stream().filter(modelElementInstance -> modelElementInstance.getElementType().getTypeName().equals(IN))
                .anyMatch(modelElementInstance -> isNotEmpty(modelElementInstance.getAttributeValue(BUSINESS_KEY)));
    }

    /**
     * Write bpmn for deployment.
     *
     * @param resource      the resource
     * @param modelInstance the model instance
     */
    private void writeBpmnForDeployment(final ResourceEntity resource, final BpmnModelInstance modelInstance) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Bpmn.writeModelToStream(outputStream, modelInstance);
        resource.setBytes(outputStream.toByteArray());
    }

    /**
     * Read resource model instance.
     *
     * @param resource the resource
     * @return the bpmn model instance
     */
    private BpmnModelInstance readResourceModelInstance(final ResourceEntity resource) {
        final byte[] bytes = resource.getBytes();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return Bpmn.readModelFromStream(inputStream);
    }

    /**
     * Checks if is process id prefixed.
     *
     * @param processDefinitionKey the process definition key
     * @param processId            the process id
     * @return true, if is process id prefixed
     */
    private boolean isProcessIdPrefixed(final String processDefinitionKey, final String processId) {
        return processId.startsWith(processDefinitionKey);
    }

    /**
     * Adds the execution start listeners for the usertasks that have the "faBackTarget" extension property set.
     *
     * @param modelInstance the modelInstance
     */
    private void addExecutionListenersForUsertaksWithBackTarget(final BpmnModelInstance modelInstance) {
        modelInstance.getModelElementsByType(UserTask.class).stream()
                .filter(this::isUsertaksWithBackTargetExtensionProperty)
                .forEach(userTask -> addExecutionStartListener(modelInstance, userTask, "com.ericsson.oss.services.flowautomation.flow.listeners.UserTaskExecutionStartListener"));
    }

    /**
     * Checks if the usertask has an extension property "faBackTarget" configured.
     *
     * @param userTask the usertask to check
     * @return the result of as boolean
     */
    private boolean isUsertaksWithBackTargetExtensionProperty(final UserTask userTask) {
        final ExtensionElements extensionElements = userTask.getExtensionElements();
        if (extensionElements == null) { //no extensions
            return false;
        }
        final Query<CamundaProperties> query = extensionElements.getElementsQuery().filterByType(CamundaProperties.class);
        if (query.count() == 0) { //no properties in extension
            return false;
        }
        return query.singleResult().getCamundaProperties().stream()
                .anyMatch(camundaProperty -> FA_BACK_TARGET.equals(camundaProperty.getCamundaName()));
    }
}
