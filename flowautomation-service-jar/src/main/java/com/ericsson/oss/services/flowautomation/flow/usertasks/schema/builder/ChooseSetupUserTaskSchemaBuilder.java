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

import static java.lang.String.format;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.USER_TASK_SCHEMA_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.FA_INTERNAL_SETUP_TYPE_USERTASK_SCHEMA_RESOURCE;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.getStringFromClassPath;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType.Value.CHOOSE_SETUP_USER_TASK_SCHEMA_BUILDER;

import java.util.Optional;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.FASchema;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.SchemaProcessorReport;
import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType;

/**
 * The Class ChooseSetupUserTaskSchemaBuilder.
 */
@UserTaskSchemaBuilderType(CHOOSE_SETUP_USER_TASK_SCHEMA_BUILDER)
public class ChooseSetupUserTaskSchemaBuilder extends UserTaskSchemaBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseSetupUserTaskSchemaBuilder.class);

    @Override
    public FASchema buildSchema(final FlowExecutionEntity flowExecutionEntity, final Task task) {
        final Optional<String> chooseSetupTaskSchema = getStringFromClassPath(FA_INTERNAL_SETUP_TYPE_USERTASK_SCHEMA_RESOURCE);
        if (chooseSetupTaskSchema.isPresent()) {
            return new FASchema(chooseSetupTaskSchema.get(), new SchemaProcessorReport(task.getName()));
        }
        LOGGER.error("User Task schema: {} not found for task: {}", task.getName(), task.getId());
        throw new FlowResourceNotFoundException(SCHEMA_NOT_FOUND, format(USER_TASK_SCHEMA_IS_NOT_FOUND, task.getId(), task.getName()));
    }
}
