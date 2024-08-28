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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder;

import static com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskAction.BACK;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskAction.EXECUTE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ACTION;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ACTIONS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DESCRIPTION;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INFORMATIONAL;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NAME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SCHEMA;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TITLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.URI_DRAFT_4;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.FLOW_INPUT_SCHEMA_FILE;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType.Value.REVIEW_CONFIRM_EXECUTE_USER_TASK_SCHEMA_BUILDER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.FASchema;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.SchemaProcessorReport;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskHelper;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.FlowInputSchemaAndDataProcessor;
import com.ericsson.oss.services.flowautomation.flow.utils.SchemaUtils;
import com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType;

/**
 * The Class ReviewAndConfirmUserTaskSchemaBuilder.
 */
@UserTaskSchemaBuilderType(REVIEW_CONFIRM_EXECUTE_USER_TASK_SCHEMA_BUILDER)
public class ReviewAndConfirmUserTaskSchemaBuilder extends UserTaskSchemaBuilder {

    private static final String FLOW_INPUT_VARIABLE = "flowInput";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewAndConfirmUserTaskSchemaBuilder.class);

    private static final String REVIEW_AND_CONFIRM_EXECUTE_USERTASK_SCHEMA = "Review and Confirm Execute usertask schema";
    private static final String REVIEW_AND_CONFIRM_EXECUTE = "Review and Confirm Execute";


    /**
     * The Flow input schema and data processor.
     */
    @Inject
    FlowInputSchemaAndDataProcessor flowInputSchemaAndDataProcessor;

    @Inject
    UserTaskHelper userTaskHelper;

    @Override
    public FASchema buildSchema(final FlowExecutionEntity flowExecutionEntity, final Task task) {
        return buildSchema(task, flowExecutionEntity, Collections.emptyMap());
    }

    /**
     * Builds the schema
     *
     * @param task         the task
     * @param nameExtraMap the map containing nameExtra items
     * @return the schema
     */
    public FASchema buildSchema(final Task task, final FlowExecutionEntity flowExecutionEntity, final Map nameExtraMap) {
        final String deploymentId = flowExecutionEntity.getFlowDetailEntity().getDeploymentId();
        LOGGER.debug("Get schema for Review and Confirm task: {}, of deployment: {}", task.getId(), deploymentId);
        final Map<String, Object> schemaMap = getSchemaMap(deploymentId, FLOW_INPUT_SCHEMA_FILE, task);
        final Map<String, Object> flowInputschemaMap = SchemaUtils.removeSchemaGenerationExtensions(schemaMap);
        flowInputSchemaAndDataProcessor.processFlowInputNameExtraList(flowInputschemaMap, nameExtraMap);

        flowInputschemaMap.put(SCHEMA, URI_DRAFT_4);
        flowInputschemaMap.put(TITLE, REVIEW_AND_CONFIRM_EXECUTE);
        flowInputschemaMap.put(NAME, REVIEW_AND_CONFIRM_EXECUTE);
        flowInputschemaMap.put(DESCRIPTION, REVIEW_AND_CONFIRM_EXECUTE_USERTASK_SCHEMA);
        flowInputschemaMap.put(FORMAT, INFORMATIONAL);

        final List<String> actions = new ArrayList<>();
        if (flowExecutionEntity.getFlowDetailEntity().isBackEnabled()) {
            final Optional<String> backTarget = userTaskHelper.getUserTaskBackTarget(flowExecutionEntity.getProcessInstanceId(), task);
            if (backTarget.isPresent()) {
                actions.add(BACK.getValue());
            }
        }
        actions.add(EXECUTE.getValue());
        flowInputschemaMap.put(ACTION, EXECUTE.getValue()); //this is to support the backward compatibility during the upgrade
        flowInputschemaMap.put(ACTIONS, actions);

        final VariableInstance flowInputVariableInstance = processEngine.getRuntimeService().createVariableInstanceQuery()
                .variableName(FLOW_INPUT_VARIABLE).processInstanceIdIn(task.getProcessInstanceId()).singleResult();
        final Map<String, Object> flowInputVariables = (Map<String, Object>) flowInputVariableInstance.getValue();
        flowInputSchemaAndDataProcessor.processFlowInputVariables(flowInputschemaMap, flowInputVariables);
        try {
            final String schema = objectMapper.writeValueAsString(flowInputschemaMap);
            return new FASchema(schema, new SchemaProcessorReport(task.getName()));
        } catch (final IOException e) {
            LOGGER.error("Error in parsing json schema: {}", e.getMessage());
            throw new FlowAutomationException("Error in parsing json schema: " + e.getMessage(), e);
        }
    }
}
