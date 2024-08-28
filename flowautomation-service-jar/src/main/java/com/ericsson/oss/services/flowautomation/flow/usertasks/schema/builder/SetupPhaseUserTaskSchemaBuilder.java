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
import static com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskAction.CONTINUE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskAction.PREVIEW;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ACTION;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ACTIONS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ADDITIONAL_PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DESCRIPTION;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INFORMATIONAL;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NAME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.REQUIRED;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SCHEMA;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TITLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.URI_DRAFT_4;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.FLOW_INPUT_SCHEMA_FILE;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType.Value.SETUP_PHASE_USER_TASK_FORM_KEY_RESOLVER;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType.Value.SETUP_PHASE_USER_TASK_SCHEMA_BUILDER;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.FASchema;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.SchemaProcessorReport;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.UserTaskSchemaAndDataProcessor;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskFormKeyResolver;
import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskHelper;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.UserTaskSchemaAndDataProcessor;
import com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType;
import com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType;

/**
 * The Class SetupPhaseUserTaskSchemaBuilder.
 */
@UserTaskSchemaBuilderType(SETUP_PHASE_USER_TASK_SCHEMA_BUILDER)
public class SetupPhaseUserTaskSchemaBuilder extends UserTaskSchemaBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetupPhaseUserTaskSchemaBuilder.class);

    private static final String USER_TASK_SCHEMA_TITLE = "User Task schema";

    /** The set up phase user task form key resolver. */
    @Inject
    @UserTaskFormKeyResolverType(SETUP_PHASE_USER_TASK_FORM_KEY_RESOLVER)
    UserTaskFormKeyResolver userTaskFormKeyResolver;

    @Inject
    UserTaskHelper userTaskHelper;

    @Inject
    UserTaskSchemaAndDataProcessor userTaskSchemaAndDataProcessor;

    @Override
    public FASchema buildSchema(final FlowExecutionEntity flowExecutionEntity, final Task task) {
        final String deploymentId = flowExecutionEntity.getFlowDetailEntity().getDeploymentId();
        LOGGER.debug("Get schema for setup phase user task: {}, of deployment: {}", task.getId(), deploymentId);

        final String usertaskPropertyKey = userTaskFormKeyResolver.resolveFormKey(task);
        final Map<String, Object> usertaskSchema;

        if (userTaskFormKeyResolver.isFormKeyExternalFile(usertaskPropertyKey)) {
            usertaskSchema = getSchemaMap(deploymentId, usertaskPropertyKey, task);
        } else {
            final Map<String, Object> flowInputSchemaProperties = (Map<String, Object>) getSchemaMap(deploymentId, FLOW_INPUT_SCHEMA_FILE, task).get(PROPERTIES);
            final Map<String, Object> usertaskPropertyValue = (Map<String, Object>) flowInputSchemaProperties.get(usertaskPropertyKey);

            usertaskSchema = getUsertaskSchemaTemplate(task);
            usertaskSchema.put(PROPERTIES, singletonMap(usertaskPropertyKey, usertaskPropertyValue));
            if (!isInformationalUsertask(usertaskPropertyValue)) {
                usertaskSchema.put(REQUIRED, new String[]{usertaskPropertyKey});
            }
            usertaskSchema.put(ADDITIONAL_PROPERTIES, false);
        }

        final List<String> actions = new ArrayList<>();
        final boolean hasBackTargetWithInProcessInstance = userTaskHelper.getUserTaskBackTarget(flowExecutionEntity.getProcessInstanceId(), task).isPresent();
        SchemaProcessorReport schemaProcessorReport = new SchemaProcessorReport(task.getName());
        if (flowExecutionEntity.getFlowDetailEntity().isBackEnabled()) {
            if (hasBackTargetWithInProcessInstance) {
                actions.add(BACK.getValue());
            }
            final Map<String, Object> previousSubmittedDataForUserTask = userTaskHelper.getPreviouslySubmittedUserTaskData(task);
            if (previousSubmittedDataForUserTask.isEmpty()) {
                schemaProcessorReport = usertaskSchemaProcessor.processSchema(usertaskSchema, task);
            } else {
                userTaskSchemaAndDataProcessor.processSchema(usertaskSchema, task, previousSubmittedDataForUserTask);
            }
        } else {
            schemaProcessorReport = usertaskSchemaProcessor.processSchema(usertaskSchema, task);
        }

        if (hasBackTargetWithInProcessInstance || userTaskHelper.isExecutionWithOtherCompletedUserTask(flowExecutionEntity, getCompletedTaskPredicate(flowExecutionEntity, task))) {
            actions.add(PREVIEW.getValue());
        }
        actions.add(CONTINUE.getValue());
        usertaskSchema.put(ACTION, CONTINUE.getValue()); //this is to support the backward compatibility during the upgrade
        usertaskSchema.put(ACTIONS, actions);
        try {
            final String schema = objectMapper.writeValueAsString(usertaskSchema);
            return new FASchema(schema, schemaProcessorReport);
        } catch (final IOException e) {
            throw new FlowAutomationException("Error in building the schema: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the completed usertask predicate that execludes the usertasks from wrapper process and current usertask process.
     * @param flowExecutionEntity the flow execution entity
     * @param task the current active usertask
     * @return the predicate to filter the task
     */
    private Predicate<HistoricTaskInstance> getCompletedTaskPredicate(final FlowExecutionEntity flowExecutionEntity, final Task task) {
        return completedTask -> !(flowExecutionEntity.getProcessInstanceId().equals(completedTask.getProcessInstanceId()) ||
                task.getProcessInstanceId().equals(completedTask.getProcessInstanceId()));
    }

    /**
     * Returns the usertask schema fixed template that includes title, name description type etc.
     *
     * @param task the usertask instance used to retrieve name and description
     * @return the usertask schema map with fixed properties
     */
    private Map<String, Object> getUsertaskSchemaTemplate(final Task task) {
        final Map<String, Object> usertaskSchema = new LinkedHashMap<>();
        usertaskSchema.put(SCHEMA, URI_DRAFT_4);
        usertaskSchema.put(TITLE, USER_TASK_SCHEMA_TITLE);
        usertaskSchema.put(NAME, task.getName() == null ? EMPTY : task.getName());
        usertaskSchema.put(DESCRIPTION, task.getDescription() == null ? EMPTY : task.getDescription());
        usertaskSchema.put(TYPE, OBJECT);
        return usertaskSchema;
    }

    /**
     * Returns true if the usertask has format as informational.
     *
     * @param usertaskPropertyValue the usertask object
     * @return true if the format is informational
     */
    private boolean isInformationalUsertask(final Map<String, Object> usertaskPropertyValue) {
        return INFORMATIONAL.equals(usertaskPropertyValue.get(FORMAT));
    }
}
