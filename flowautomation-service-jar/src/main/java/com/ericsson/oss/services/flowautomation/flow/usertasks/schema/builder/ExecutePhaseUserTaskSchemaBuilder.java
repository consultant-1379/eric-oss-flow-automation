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

import static com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskAction.SUBMIT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ACTION;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ACTIONS;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType.Value.EXECUTE_PHASE_USER_TASK_FORM_KEY_RESOLVER;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType.Value.EXECUTE_PHASE_USER_TASK_SCHEMA_BUILDER;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskFormKeyResolver;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.FASchema;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.SchemaProcessorReport;
import com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType;
import com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType;

/**
 * The Class ExecutePhaseUserTaskSchemaBuilder.
 */
@UserTaskSchemaBuilderType(EXECUTE_PHASE_USER_TASK_SCHEMA_BUILDER)
public class ExecutePhaseUserTaskSchemaBuilder extends UserTaskSchemaBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutePhaseUserTaskSchemaBuilder.class);

    /** The execute phase user task form key resolver. */
    @Inject
    @UserTaskFormKeyResolverType(EXECUTE_PHASE_USER_TASK_FORM_KEY_RESOLVER)
    UserTaskFormKeyResolver userTaskFormKeyResolver;

    @Override
    public FASchema buildSchema(final FlowExecutionEntity flowExecutionEntity, final Task task) {
        final String deploymentId = flowExecutionEntity.getFlowDetailEntity().getDeploymentId();
        LOGGER.debug("Get schema for execution phase user task: {}, of deployment: {}", task.getId(), deploymentId);
        final String resourceName = userTaskFormKeyResolver.resolveFormKey(task);
        final Map<String, Object> userTaskSchemaMap = getSchemaMap(deploymentId, resourceName, task);
        userTaskSchemaMap.put(ACTION, SUBMIT.getValue()); //this is to support the backward compatibility during the upgrade
        userTaskSchemaMap.put(ACTIONS, singletonList(SUBMIT.getValue()));

        final SchemaProcessorReport schemaProcessorReport = usertaskSchemaProcessor.processSchema(userTaskSchemaMap, task);

        try {
            final String schema = objectMapper.writeValueAsString(userTaskSchemaMap);
            return new FASchema(schema, schemaProcessorReport);
        } catch (final IOException e) {
            LOGGER.error("Error in parsing json schema: {}", e.getMessage());
            throw new FlowAutomationException("Error parsing json schema: " + e.getMessage(), e);
        }
    }
}
