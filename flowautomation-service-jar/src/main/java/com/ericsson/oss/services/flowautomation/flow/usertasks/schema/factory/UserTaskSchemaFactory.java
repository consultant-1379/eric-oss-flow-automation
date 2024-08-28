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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.factory;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.OPERATION_IS_NOT_ALLOWED_ON_STATE;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_CONFLICT;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.CHOOSE_SETUP_USER_TASK;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.REVIEW_AND_CONFIRM_EXECUTE_USER_TASK;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType.Value.CHOOSE_SETUP_USER_TASK_SCHEMA_BUILDER;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType.Value.EXECUTE_PHASE_USER_TASK_SCHEMA_BUILDER;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType.Value.REVIEW_CONFIRM_EXECUTE_USER_TASK_SCHEMA_BUILDER;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType.Value.SETUP_PHASE_USER_TASK_SCHEMA_BUILDER;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.exception.OperationNotAllowedException;
import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskHelper;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.ReviewAndConfirmUserTaskSchemaBuilder;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.UserTaskSchemaBuilder;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.FASchema;
import com.ericsson.oss.services.flowautomation.resources.UserTaskSchemaBuilderType;

/**
 * A factory for creating UserTaskSchema.
 */
@ApplicationScoped
public class UserTaskSchemaFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskSchemaFactory.class);

    @Inject
    UserTaskHelper userTaskHelper;

    @Inject
    @UserTaskSchemaBuilderType(CHOOSE_SETUP_USER_TASK_SCHEMA_BUILDER)
    private UserTaskSchemaBuilder chooseSetupUserTaskSchemaBuilder;

    @Inject
    @UserTaskSchemaBuilderType(REVIEW_CONFIRM_EXECUTE_USER_TASK_SCHEMA_BUILDER)
    private UserTaskSchemaBuilder reviewConfirmUserTaskSchemaBuilder;

    @Inject
    @UserTaskSchemaBuilderType(SETUP_PHASE_USER_TASK_SCHEMA_BUILDER)
    private UserTaskSchemaBuilder setupPhaseUserTaskSchemaBuilder;

    @Inject
    @UserTaskSchemaBuilderType(EXECUTE_PHASE_USER_TASK_SCHEMA_BUILDER)
    private UserTaskSchemaBuilder executePhaseUserTaskSchemaBuilder;

    /**
     * Builds the schema.
     *
     * @param flowExecutionEntity
     * @param flowExecutionPhase
     * @param task                the task
     * @return the string
     */
    public FASchema buildSchema(final FlowExecutionEntity flowExecutionEntity, final String flowExecutionPhase, final Task task) {
        LOGGER.debug("Build schema for task id: {}, name: {}", task.getId(), task.getName());
        final String taskDefinitionKey = task.getTaskDefinitionKey();
        if (taskDefinitionKey.equals(CHOOSE_SETUP_USER_TASK.getId())) {
            return chooseSetupUserTaskSchemaBuilder.buildSchema(flowExecutionEntity, task);
        } else if (taskDefinitionKey.equals(REVIEW_AND_CONFIRM_EXECUTE_USER_TASK.getId())) {
            return ((ReviewAndConfirmUserTaskSchemaBuilder) reviewConfirmUserTaskSchemaBuilder).buildSchema(task, flowExecutionEntity, userTaskHelper.getNameExtraMap(flowExecutionEntity));
        } else {
            switch (State.valueOf(flowExecutionPhase)) {
                case SETTING_UP:
                    return setupPhaseUserTaskSchemaBuilder.buildSchema(flowExecutionEntity, task);
                case EXECUTING:
                    return executePhaseUserTaskSchemaBuilder.buildSchema(flowExecutionEntity, task);
                case STOPPED:
                case STOPPING:
                case STOP:
                    LOGGER.error("Cannot perform this operation when state is {}", flowExecutionPhase);
                    throw new OperationNotAllowedException(FLOW_CONFLICT, String.format(OPERATION_IS_NOT_ALLOWED_ON_STATE, task.getId(), flowExecutionPhase));
                default:
                    LOGGER.error("Cannot get usertask schema when execution state is {}", flowExecutionPhase);
                    throw new FlowAutomationException(String.format("Cannot get usertask schema when execution state is %s", flowExecutionPhase));
            }
        }
    }
}
