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

package com.ericsson.oss.services.flowautomation.flow;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntitySource.EXTERNAL;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntityStatus.ENABLED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_CAN_NOT_BE_DELETED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_EXECUTION_FAILED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_EXECUTION_NAME_ALREADY_EXISTS;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_ID_DOES_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_PACKAGE_ALREADY_EXISTS;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_PACKAGE_DEPLOYMENT_FAILED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_VERSION_IS_NOT_ALLOWED;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_ALREADY_EXISTS;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_DOES_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_CANNOT_BE_DELETED;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_FAILED_ERROR;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_NAME_IN_USE;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_INPUT;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_CANNOT_BE_IMPORTED;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEPLOYMENT_FAILED;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_VERSION_NOT_ALLOWED;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationServiceUtil.isVersionSyntaxValid;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.EXECUTE_CALL_ACTIVITY;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.FA_INTERNAL_USER_SETUP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.FLOW_INPUT;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.SETUP_CALL_ACTIVITY;
import static java.lang.String.format;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.deployment.processor.FlowAutomationDeploymentProcessor;
import com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity;
import com.ericsson.oss.services.flowautomation.entities.FlowEntity;
import com.ericsson.oss.services.flowautomation.entities.FlowEntitySource;
import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.error.ErrorMessageContants;
import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;
import com.ericsson.oss.services.flowautomation.exception.EntityExistsException;
import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.exception.JsonSchemaValidationException;
import com.ericsson.oss.services.flowautomation.exception.ValidationException;
import com.ericsson.oss.services.flowautomation.flow.definition.FlowConfig;
import com.ericsson.oss.services.flowautomation.flow.definition.FlowDefinitionParser;
import com.ericsson.oss.services.flowautomation.flow.definition.FlowSetupConfig;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowServiceException;
import com.ericsson.oss.services.flowautomation.flow.exception.OperationNotSupportedException;
import com.ericsson.oss.services.flowautomation.flow.setting.FlowInputSchemaAndDataBuilder;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil;
import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionWrapperGenerator;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionInput;
import com.ericsson.oss.services.flowautomation.model.FlowSource;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.google.common.base.Strings;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * Flow Service implementation
 */
@ServiceType(JAR_TYPE)
public class FlowServiceImpl extends AbstractFlowAutomationService implements FlowService {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowServiceImpl.class);

    /** The flow automation deployment processor. */
    @Inject
    private FlowAutomationDeploymentProcessor flowAutomationDeploymentProcessor;

    @Inject
    private FlowInputSchemaAndDataBuilder flowInputSchemaAndDataBuilder;

    private FlowDefinition importFlow(final byte[] flowPackage, final FlowEntitySource source) {
        final FlowConfig flowConfig = FlowDefinitionParser.parseFlowDefinitionJson(flowPackage);
        final String flowId = flowConfig.getFlowId();
        final String flowVersion = flowConfig.getFlowVersion();
        final String name = flowConfig.getFlowName();
        LOGGER.debug("Deploying flow, id: {}, name: {}, flowversion: {}", flowId, name, flowVersion);
        final String importedByUser = getUserInContext();

        //This is to address the issue of unique flow name across the application
        if (flowRepository.isFlowNameInUseByAnotherFlow(flowId, name)) {
            LOGGER.info("flow name: {} is already in use by another flow.", name);
            throw new FlowServiceException(FlowPackageErrorCode.FLOW_NAME_ALREADY_INUSE, format(ErrorMessageContants.FLOW_NAME_ALREADY_INUSE, name));
        }

        /* Get FlowEntity and create FlowDetailEntity */
        final FlowEntity flowEntity = getFlowEntity(flowConfig, source);
        final List<FlowDetailEntity> flowDetailEntities = flowEntity.getFlowDetailEntities();

        validateFlowVersion(flowId, flowVersion, flowDetailEntities);

        final String processDefinitionKey = ExecutionWrapperGenerator.getWrapperProcessId(flowId, flowVersion);
        LOGGER.debug("Process Definition key: {}", processDefinitionKey);

        /* Deploy the flow */
        final FlowDetailEntity flowDetailEntity;

        try {
            flowDetailEntity = createFlowDetailEntity(flowPackage, flowConfig, flowEntity, processDefinitionKey, importedByUser);
        } catch (final FlowResourceNotFoundException e) {
            LOGGER.error("flow deployment failed: {}", String.valueOf(e));
            throw new FlowServiceException(FLOW_DEPLOYMENT_FAILED, format(FLOW_PACKAGE_DEPLOYMENT_FAILED, flowId, name, flowVersion), e);
        }

        final DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment();
        deploymentBuilder.name(flowId);
        deploymentBuilder.enableDuplicateFiltering(false);

        try (final ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(flowPackage))) {
            deploymentBuilder.addZipInputStream(zipStream);
            final BpmnModelInstance wrapperProcess = ExecutionWrapperGenerator.generateWrapperProcess(flowConfig);
            final String wrapperProcessResourceName = ExecutionWrapperGenerator.getWrapperProcessResourceName(flowId);
            deploymentBuilder.addModelInstance(wrapperProcessResourceName, wrapperProcess);

            flowAutomationDeploymentProcessor.processDeployment(processDefinitionKey, deploymentBuilder);

            final String deploymentId = deploymentBuilder.deploy().getId();
            flowDetailEntity.setDeploymentId(deploymentId);
            flowRepository.save(flowEntity);
            LOGGER.debug("Deployed flow, name: [{}], id: [{}]", flowId, deploymentId);
            return flowDetailEntity.getFlowDefinition();
        } catch (final ProcessEngineException | IOException e) {
            LOGGER.error("flow deployment failed: {}", String.valueOf(e));
            throw new FlowServiceException(FLOW_DEPLOYMENT_FAILED, format(FLOW_PACKAGE_DEPLOYMENT_FAILED, flowId, name, flowVersion), e);
        } catch (final JsonSchemaValidationException e){
            LOGGER.error("flow import failed: {}", String.valueOf(e));
            throw new ValidationException(FLOW_CANNOT_BE_IMPORTED, e.getMessage());
        }
    }

    /**
     * Gets the flow entity.
     *
     * @param flowConfig the flow config
     * @return the flow entity
     */
    private FlowEntity getFlowEntity(final FlowConfig flowConfig, final FlowEntitySource source) {
        FlowEntity flowEntity = flowRepository.getFlowEntity(flowConfig.getFlowId());
        final String flowName = flowConfig.getFlowName();
        if (flowEntity == null) {
            flowEntity = new FlowEntity();
            flowEntity.setFlowId(flowConfig.getFlowId());
            flowEntity.setName(flowName);
            flowEntity.setSource(source);

            /* Flow is enabled now. But distinguish between predefined flows and customer flows, then set the value. */
            flowEntity.setStatus(ENABLED.getStatus());

            final List<FlowDetailEntity> flowDetailEntities = new ArrayList<>();
            flowEntity.setFlowDetailEntities(flowDetailEntities);
        }
        if (!flowEntity.getName().equals(flowName)) {
            flowEntity.setName(flowName);
        }
        return flowEntity;
    }

    /**
     * Creates the flow detail entity.
     *
     * @param flowPackage         the flow package
     * @param flowConfig           the flow config
     * @param flowEntity           the flow entity
     * @param processDefinitionKey the process definition key
     * @param importedByUser       the imported by user
     * @return the flow detail entity
     */
    private FlowDetailEntity createFlowDetailEntity(final byte[] flowPackage, final FlowConfig flowConfig, final FlowEntity flowEntity, final String processDefinitionKey,
                                                    final String importedByUser) {
        final FlowDetailEntity flowDetailEntity = new FlowDetailEntity();
        flowDetailEntity.setProcessDefinitionKey(processDefinitionKey);
        flowDetailEntity.setDescription(flowConfig.getDescription());
        flowDetailEntity.setFlowEntity(flowEntity);
        flowDetailEntity.setImportedByUser(importedByUser);

        flowDetailEntity.setVersion(flowConfig.getFlowVersion());
        final FlowSetupConfig setupConfig = flowConfig.getSetup();
        if (setupConfig != null) {
            flowDetailEntity
                    .setSetupId(flowAutomationDeploymentProcessor.generateProcessId(processDefinitionKey, setupConfig.getProcessId()));
            flowDetailEntity.setBackEnabled(Objects.nonNull(setupConfig.getUserTask()) && setupConfig.getUserTask().isBackEnabled());
        }

        // check flow zip for flow-report-schema.json file
        try (final ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(flowPackage))) {
            flowDetailEntity.setIsReportSupported(FlowPackageUtil.checkZipForFlowReportSchema(zipStream));
        } catch (final FlowResourceNotFoundException | IOException e) {
            throw new FlowResourceNotFoundException(e);
        }

        flowDetailEntity
                .setExecuteId(flowAutomationDeploymentProcessor.generateProcessId(processDefinitionKey, flowConfig.getExecute().getProcessId()));
        /*
         * Flow version is inactive now. But distinguish between predefined flows and customer flows, then set the value. Mark latest flow as active
         * as described by [TORF-291394].
         */
        flowDetailEntity.setIsActive(true);
        // De-activating previously installed flow versions.
        flowEntity.getFlowDetailEntities().stream().filter(FlowDetailEntity::getIsActive).findFirst().ifPresent(e -> e.setIsActive(false));
        flowEntity.getFlowDetailEntities().add(flowDetailEntity);

        return flowDetailEntity;
    }

    /**
     * Validate flow version.
     *
     * @param flowId             the flow id
     * @param flowVersion        the flow version
     * @param flowDetailEntities the flow detail entities
     */
    private void validateFlowVersion(final String flowId, final String flowVersion, final List<FlowDetailEntity> flowDetailEntities) {
        if (isVersionSyntaxValid(flowVersion)) {
            final int[] versionToDeploy = convertFlowVersion(flowVersion);
            flowDetailEntities.forEach(flowDetailEntity -> {
                final int[] existingVersion = convertFlowVersion(flowDetailEntity.getVersion());
                if (Arrays.equals(versionToDeploy, existingVersion)) {
                    LOGGER.debug("Flow package with id: {}, flowversion: {} already exists.", flowId, flowVersion);
                    throw new EntityExistsException(FLOW_ALREADY_EXISTS, format(FLOW_PACKAGE_ALREADY_EXISTS, flowId, flowVersion));
                } else if (isVersionLowerThanExisting(versionToDeploy, existingVersion)) {
                    LOGGER.debug("Flow package with id: {} contains lower flowversion: {}", flowId, flowVersion);
                    throw new FlowServiceException(FLOW_VERSION_NOT_ALLOWED, String.format(FLOW_VERSION_IS_NOT_ALLOWED, flowId, flowVersion));
                }
            });

        }
    }

    /**
     * Convert flow version.
     *
     * @param flowVersion the flow version
     * @return the flow version
     */
    private int[] convertFlowVersion(final String flowVersion) {
        final String[] splitVersion = flowVersion.split("\\.");
        final int[] version = new int[splitVersion.length];
        for (int i = 0; i < splitVersion.length; i++) {
            version[i] = Integer.parseInt(splitVersion[i]);
        }
        return version;
    }

    /**
     * Checks if is version lower than existing.
     *
     * @param versionToDeploy the version to deploy
     * @param existingVersion the existing version
     * @return true, if is version lower than existing
     */
    private boolean isVersionLowerThanExisting(final int[] versionToDeploy, final int[] existingVersion) {
        for (int i = 0; i < versionToDeploy.length; i++) {
            final int versionToDeployBit = versionToDeploy[i];
            final int existingVersionBit = existingVersion[i];
            if (versionToDeployBit < existingVersionBit) {
                return true;
            } else if (versionToDeployBit > existingVersionBit) {
                return false;
            }
        }
        return true;
    }

    /**
     * Execute flow.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @param flowInputData     the flow input data
     */
    private void executeFlow(final String flowId, final String flowExecutionName, final String flowInputData,
                             final Map<String, byte[]> flowInputFiles) {
        validateFlowId(flowId);

        /* Check flowExecutionName exists already. */
        if (flowExecutionRepository.flowExecutionNameExists(flowId, flowExecutionName)) {
            LOGGER.debug("Flow Execution with the flowId: {} and name: {} already exists.", flowId, flowExecutionName);
            throw new EntityExistsException(FLOW_EXECUTION_NAME_IN_USE, format(FLOW_EXECUTION_NAME_ALREADY_EXISTS, flowExecutionName, flowId));
        }
        /* Get process definition key for active flow version */
        final FlowDetailEntity flowDetailEntity = flowDetailRepository.getActiveFlowVersion(flowId);
        final String processDefinitionKey = flowDetailEntity.getProcessDefinitionKey();
        final String setupId = flowDetailEntity.getSetupId();
        final String executionId = flowDetailEntity.getExecuteId();

        final RuntimeService runtimeService = processEngine.getRuntimeService();
        final Map<String, Object> variables = new HashMap<>();
        if (setupId != null) {
            variables.put(SETUP_CALL_ACTIVITY.getCalledElementId(), setupId);
            variables.put("FAReviewAndConfirmExecuted", false);
        }
        variables.put(EXECUTE_CALL_ACTIVITY.getCalledElementId(), executionId);

        if (!StringUtils.isEmpty(flowInputData)) {
            variables.put(FLOW_INPUT.getName(),
                    getFlowInputVariables(flowId, flowDetailEntity.getVersion(), flowDetailEntity.getDeploymentId(), flowInputData, flowInputFiles));
            variables.put(FA_INTERNAL_USER_SETUP.getName(), false);
        }

        try {
            /* Create flow execution entity */
            final String executedByUser = getUserInContext();
            final FlowExecutionEntity flowExecutionEntity = createFlowExecutionEntity(flowDetailEntity, flowExecutionName, executedByUser);
            flowExecutionRepository.saveAndFlush(flowExecutionEntity);
            final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(flowId, flowExecutionName, flowExecutionEntity.getId().toString());
            final String businessKey = flowBusinessKey.getCamundaBusinessKey();
            final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
            flowExecutionEntity.setProcessInstanceId(processInstance.getId());
            flowExecutionEntity.setProcessInstanceBusinessKey(processInstance.getBusinessKey());
            flowExecutionRepository.update(flowExecutionEntity);
        } catch (final ProcessEngineException e) {
            LOGGER.error("flow execution failed", e);
            throw new FlowServiceException(FLOW_EXECUTION_FAILED_ERROR, format(FLOW_EXECUTION_FAILED, flowId, flowExecutionName), e);
        }
    }

    /**
     * Creates the flow execution entity.
     *
     * @param flowExecutionName the flow execution name
     * @return the flow execution entity
     */
    private FlowExecutionEntity createFlowExecutionEntity(final FlowDetailEntity flowDetailEntity, final String flowExecutionName,
                                                          final String executedByUser) {
        final FlowExecutionEntity flowExecutionEntity = new FlowExecutionEntity();
        flowExecutionEntity.setExecutedByUser(executedByUser);
        flowExecutionEntity.setFlowExecutionName(flowExecutionName);
        flowExecutionEntity.setFlowDetailEntity(flowDetailEntity);
        flowDetailEntity.addFlowExecutionEntity(flowExecutionEntity);
        flowExecutionEntity.setProcessInstanceId("");
        return flowExecutionEntity;
    }

    @Override
    public List<FlowDefinition> getFlows() {
        return flowRepository.findAll().stream().map(FlowEntity::getFlowDefinition).collect(Collectors.toList());
    }

    @Override
    public FlowDefinition importFlow(final byte[] flowPackage) {
        return importFlow(flowPackage, EXTERNAL);
    }

    @Override
    public FlowDefinition importFlow(final byte[] flowPackage, final FlowSource flowSource) {
        return importFlow(flowPackage, FlowEntitySource.from(flowSource));
    }

    @Override
    public void executeFlow(final String flowId, final String flowExecutionName) {
        LOGGER.debug("Executing flow id: {}, flowExecutionName: {}", flowId, flowExecutionName);
        executeFlow(flowId, flowExecutionName, null, null);
    }

    @Override
    public void executeFlow(final String flowId, final FlowExecutionInput flowExecutionInput) {
        if (flowExecutionInput.getFlowInput().isEmpty() && flowExecutionInput.getFlowInputFiles().isEmpty()){
            throw new ValidationException(INVALID_FLOW_INPUT, format(INVALID_FLOW_INPUT.reason(), flowId));
        }
        LOGGER.debug("Executing flow with input: {}", flowExecutionInput);
        executeFlow(flowId, flowExecutionInput.getName(), flowExecutionInput.getFlowInput(), flowExecutionInput.getFlowInputFiles());
    }

    @Override
    @SuppressWarnings({"squid:S3776", "squid:S135"})
    public void activateFlowVersion(final String flowId, final String flowVersion, final boolean activate) {
        /*
         * Check flowId and version does exist. Though it is PUT request, we cannot create a resource if does not exist. PATCH is supported in JAX-RS
         * 2.1 available in JEE 8.
         */
        validateFlowId(flowId);
        validateFlowVersion(flowId, flowVersion);
        final String processDefinitionKey = ExecutionWrapperGenerator.getWrapperProcessId(flowId, flowVersion);
        final FlowEntity flowEntity = flowRepository.getFlowEntity(flowId);
        boolean result = false;
        for (final FlowDetailEntity flowDetailEntity : flowEntity.getFlowDetailEntities()) {
            if (flowDetailEntity.getProcessDefinitionKey().equals(processDefinitionKey)) {
                final boolean isActive = flowDetailEntity.getIsActive();
                if (isActive == activate) {
                    break;
                } else {
                    flowDetailEntity.setIsActive(activate);
                    result = true;
                    if (!activate) {
                        break;
                    }
                }
            } else if (activate && flowDetailEntity.getIsActive()) {
                flowDetailEntity.setIsActive(!activate);
                if (result) {
                    break;
                }
            }
        }
    }

    @Override
    public void enableFlow(final String flowId, final boolean enable) {
        validateFlowId(flowId);
        flowRepository.updateFlowStatus(flowId, enable);
    }

    @Override
    public FlowDefinition getFlowDefinition(final String flowId) {
        final FlowEntity flowEntity = flowRepository.getFlowEntity(flowId);
        if (flowEntity == null) {
            LOGGER.debug("Flow: {} does not exist.", flowId);
            throw new EntityNotFoundException(FLOW_DOES_NOT_EXIST, format(FLOW_ID_DOES_NOT_EXIST, flowId));
        }
        return flowEntity.getFlowDefinition();
    }

    @Override
    public String getFlowInputSchema(final String flowId, final String flowVersion) {
        String flowInputSchema = "";
        FlowDetailEntity flowDetailEntity = null;
        validateFlowId(flowId);
        if (!Strings.isNullOrEmpty(flowVersion)) {
            validateFlowVersion(flowId, flowVersion);
            flowDetailEntity = flowDetailRepository.getFlowVersion(flowId, flowVersion);
        } else {
            flowDetailEntity = flowDetailRepository.getActiveFlowVersion(flowId);
        }

        flowInputSchema = getFlowInputSchema(flowDetailEntity);

        return flowInputSchema;
    }

    @Override
    public void deleteFlow(final String flowId, final boolean force) {
        if (!force) {
            LOGGER.debug("Flow cannot be deleted: {}", flowId);
            throw new OperationNotSupportedException(FLOW_CANNOT_BE_DELETED, format(FLOW_CAN_NOT_BE_DELETED, flowId));
        }
        validateFlowId(flowId);
        LOGGER.debug("Deleting flow with id: {}", flowId);
        final FlowEntity flowEntity = flowRepository.getFlowEntity(flowId);
        flowEntity.getFlowDetailEntities().forEach(
                flowDetailEntity -> processEngine.getRepositoryService().deleteDeployment(flowDetailEntity.getDeploymentId(), true, true, true));
        flowRepository.delete(flowEntity);
    }

    @Override
    public FlowVersionProcessDetails getProcessDetailsForFlowVersion(final String flowId, final String flowVersion) {
        validateFlowId(flowId);
        validateFlowVersion(flowId, flowVersion);

        final FlowDetailEntity flowDetailEntity = flowDetailRepository.getFlowVersion(flowId, flowVersion);

        final String processDefinitionKey = flowDetailEntity.getProcessDefinitionKey();
        final ProcessDefinition processDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).singleResult();

        return new FlowVersionProcessDetails(processDefinition.getKey(), flowDetailEntity.getSetupId(), flowDetailEntity.getExecuteId());
    }

    private String getFlowInputSchema(final FlowDetailEntity flowDetailEntity) {
        return flowInputSchemaAndDataBuilder.getFlowInputSchemaAsString(flowDetailEntity.getDeploymentId());
    }

}
