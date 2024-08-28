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

package com.ericsson.oss.services.flowautomation.flow.setting;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_INPUT_DATA_NOT_AVAILABLE_SETUP_PHASE_ERROR;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.SCHEMA_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SETUP_PHASE_AMBIGUOUS_ACTIVE_USERTASKS;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SETUP_PHASE_ERROR;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DESCRIPTION;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INFORMATIONAL;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NAME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SCHEMA;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TITLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.URI_DRAFT_4;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.cache.SchemaCache.SCHEMA_CACHE;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.FLOW_INPUT_SCHEMA_FILE;
import static com.ericsson.oss.services.flowautomation.flow.utils.SchemaUtils.getSchemaMap;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.FA_INTERNAL_FLOW_INPUT_DATA;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.FLOW_INPUT;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.FlowExecutionPhase;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskHelper;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.FlowInputSchemaAndDataProcessor;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader;
import com.ericsson.oss.services.flowautomation.flow.utils.SchemaUtils;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The type Flow input schema and data builder.
 */
public class FlowInputSchemaAndDataBuilder {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowInputSchemaAndDataBuilder.class);

    private static final String FLOW_INSTANCE_INPUT_DATA = "Flow Instance Input Data";

    private static final String FLOW_INSTANCE_INPUT_DATA_DESCRIPTION = "Flow Instance Input Data supplied in Setup Phase";

    @Inject
    protected FlowResourceLoader flowResourceLoader;

    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    @Inject
    UserTaskHelper userTaskHelper;

    @Inject
    private FlowExecutionPhase flowExecutionPhase;

    @Inject
    FlowInputSchemaAndDataProcessor flowInputSchemaAndDataProcessor;

    /**
     * Gets flow input schema.
     *
     * @param deploymentId the deployment id
     * @return the flow input schema
     */
    public String getFlowInputSchemaAsString(final String deploymentId) {
        try {
            return objectMapper.writeValueAsString(getFlowInputSchemaMap(deploymentId));
        } catch (final IOException e) {
            throw new FlowAutomationException("Error in parsing json schema of process instance: " + e.getMessage(), e);
        }
    }

    /**
     * Gets flow input schema and data.
     *
     * @return the flow input schema and data
     */
    public String getSerializedFlowInputSchemaWithData(final FlowExecutionEntity flowExecutionEntity) {
        try {
            return objectMapper.writeValueAsString(getFlowInputSchemaData(flowExecutionEntity));
        } catch (final IOException e) {
            LOGGER.error("Error in parsing json schema of process instance: {} and deployment: {}. Exception: {}", flowExecutionEntity.getProcessInstanceId(),
                    flowExecutionEntity.getFlowDetailEntity().getDeploymentId(), e.getMessage());
            throw new FlowAutomationException("Error in parsing json schema of process instance: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getFlowInputSchemaData(final FlowExecutionEntity flowExecutionEntity) {
        final Map<String, String> nameExtras = userTaskHelper.getNameExtraMap(flowExecutionEntity);

        Map<String, Object> schemaMap = getFlowInputSchemaMap(flowExecutionEntity.getFlowDetailEntity().getDeploymentId());
        try {
            final Map<String, Object> schemaMapCopy = objectMapper.readValue(objectMapper.writeValueAsString(schemaMap), Map.class);
            final Map<String, Object> flowInputschemaMap = SchemaUtils.removeSchemaGenerationExtensions(schemaMapCopy);
            flowInputSchemaAndDataProcessor.processFlowInputNameExtraList(flowInputschemaMap, nameExtras);

            flowInputschemaMap.put(SCHEMA, URI_DRAFT_4);
            flowInputschemaMap.put(TITLE, FLOW_INSTANCE_INPUT_DATA);
            flowInputschemaMap.put(NAME, FLOW_INSTANCE_INPUT_DATA);
            flowInputschemaMap.put(DESCRIPTION, FLOW_INSTANCE_INPUT_DATA_DESCRIPTION);
            flowInputschemaMap.put(FORMAT, INFORMATIONAL);

            final Map<String, Object> flowInputVariables = getFlowInputVariables(flowExecutionEntity);
            flowInputSchemaAndDataProcessor.processFlowInputVariables(flowInputschemaMap, flowInputVariables);
            return flowInputschemaMap;
        } catch (final IOException e) {
            throw new FlowAutomationException("Error parsing json schema: " + e.getMessage(), e);
        }
    }

    /**
     * Gets flow input schema.
     *
     * @param deploymentId the deployment id
     * @return the flow input schema Map
     */
    public Map<String, Object> getFlowInputSchemaMap(final String deploymentId) {
        String schema = SCHEMA_CACHE.get(deploymentId, FLOW_INPUT_SCHEMA_FILE);
        Map<String, Object> schemaMap = null;
        if (schema == null) {
            final Optional<String> jsonSchema = flowResourceLoader.getFlowResourceByDeploymentId(deploymentId,
                    FLOW_INPUT_SCHEMA_FILE);
            if (jsonSchema.isPresent()) {
                String actualSchema = jsonSchema.get();
                SCHEMA_CACHE.put(deploymentId, FLOW_INPUT_SCHEMA_FILE, actualSchema);
                LOGGER.debug("Schema: {} of deployment: {} retrieved from DB, cache it.", FLOW_INPUT_SCHEMA_FILE,
                        deploymentId);
                schemaMap = getSchemaMap(actualSchema);
            } else {
                LOGGER.error("No schema: {} found of deployment: {}", FLOW_INPUT_SCHEMA_FILE, deploymentId);
                throw new FlowResourceNotFoundException(SCHEMA_NOT_FOUND,
                        String.format(SCHEMA_IS_NOT_FOUND, FLOW_INPUT_SCHEMA_FILE));
            }
        } else {
            schemaMap = getSchemaMap(schema);
        }
        return schemaMap;
    }


    /**
     * Gets flow input data.
     *
     * @param processInstanceId the process instance id
     * @return the flow input data
     */
    public Map<String, Object> getFlowInputData(final String processInstanceId) {
        final HistoricVariableInstance flowInputVariableInstance = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .variableName(FA_INTERNAL_FLOW_INPUT_DATA.getName()).processInstanceId(processInstanceId).singleResult();
        if (flowInputVariableInstance == null) {
            throw new FlowResourceNotFoundException(SETUP_PHASE_ERROR, FLOW_INPUT_DATA_NOT_AVAILABLE_SETUP_PHASE_ERROR);
        }
        return (Map<String, Object>) flowInputVariableInstance.getValue();
    }


    /**
     * Gets flow input by reading FLOW_INPUT variable from the flow execution.
     *
     * @param processInstanceId the process instance id
     * @return the flow input data in review confirm phase
     */
    public Map<String, Object> getFlowInput(final String processInstanceId) {
        final VariableInstance flowInputVariable = processEngine.getRuntimeService().createVariableInstanceQuery()
                .variableName(FLOW_INPUT.getName()).processInstanceIdIn(processInstanceId).singleResult();

        return flowInputVariable == null ? Collections.emptyMap():(Map < String, Object >) flowInputVariable.getValue();
    }


    /**
     * In setup phase, it reads the flowInput from the process instance which has an active usertask.
     * Also, there should be exactly one active usertask when the flowInput can be queried in setup phase of the execution.
     *
     * In Execution phase, it return the flowInput by querying the copied FAInternalFlowInputData.
     *
     * @param flowExecutionEntity the flow execution
     * @return the map of flowInput variables
     */
    private Map<String, Object> getFlowInputVariables(final FlowExecutionEntity flowExecutionEntity) {
        if (flowExecutionPhase.isExecutionInSetupPhase(flowExecutionEntity.getProcessInstanceId())) {
            final String businessKey = new FlowBusinessKey(flowExecutionEntity.getFlowId(),
                    flowExecutionEntity.getFlowExecutionName(),
                    flowExecutionEntity.getId().toString())
                    .getCamundaBusinessKey();

            final List<Task> activeTasks = processEngine.getTaskService().createTaskQuery()
                    .processInstanceBusinessKey(businessKey)
                    .active()
                    .list();
            if(activeTasks.size() != 1){
                LOGGER.info("There are {} active usertasks in the setup phase of execution and the flowInput won't be available", activeTasks.size());
                throw new FlowResourceNotFoundException(SETUP_PHASE_AMBIGUOUS_ACTIVE_USERTASKS, "The flow input cannot be queried at this stage of flow execution.");
            }
            return getFlowInput(activeTasks.get(0).getProcessInstanceId());
        }
        return getFlowInputData(flowExecutionEntity.getProcessInstanceId());
    }
}
