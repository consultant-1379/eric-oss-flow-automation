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

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.USER_TASK_SCHEMA_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.USER_TASK_SCHEMA_NOT_FOUND;

import java.util.Map;

import javax.inject.Inject;

import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.FASchema;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.UserTaskSchemaProcessor;

public abstract class UserTaskSchemaBuilder extends SchemaBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskSchemaBuilder.class);

    /** The usertask schema processor. */
    @Inject
    protected UserTaskSchemaProcessor usertaskSchemaProcessor;

    /**
     * Builds the schema.
     *
     * @param task the task
     * @return the schema
     */
    public abstract FASchema buildSchema(FlowExecutionEntity flowExecutionEntity, Task task);

}
