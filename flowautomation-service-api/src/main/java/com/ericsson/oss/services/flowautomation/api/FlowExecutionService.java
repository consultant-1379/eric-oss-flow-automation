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

package com.ericsson.oss.services.flowautomation.api;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEvent;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.model.PaginatedData;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.model.UserTaskSchema;

/**
 * The interface Flow execution service.
 */
public interface FlowExecutionService {

    /**
     * Returns the collection of executions by applying the specified filter {@code FlowExecutionFilter}.
     *
     * @param filter the filter to apply on the result set
     * @return the executions the list of execution matching the filter
     */
    List<FlowExecution> getExecutions(final FlowExecutionFilter filter);

    /**
     * Gets user tasks.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @return the user tasks
     */
    List<UserTask> getUserTasks(final String flowId, final String flowExecutionName);

    /**
     * Complete user task.
     *
     * @param taskId    the task id
     * @param variables the variables
     */
    void completeUserTask(final String taskId, final Map<String, Object> variables);

    /**
     * Complete user task.
     *
     * @param taskId            the task id
     * @param userTaskFileInput the user task file input
     * @param usertTaskInput    the usert task input
     */
    void completeUserTask(final String taskId, final Map<String, byte[]> userTaskFileInput, final String usertTaskInput);


    /**
     * Cancels the current usertask and takes back to the previously completed usertask or the one configured using faBackTarget property in the usertask extensions.
     *
     * @param taskId the task id
     */
    void revertUserTask(final String taskId);

    /**
     * Gets user task schema.
     *
     * @param taskId the task id
     * @return the user task schema
     */
    UserTaskSchema getUserTaskSchema(final String taskId);

    /**
     * Delete execution.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     */
    void deleteExecution(final String flowId, final String flowExecutionName);

    /**
     * Gets execution report.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @return the execution report
     */
    String getExecutionReport(final String flowId, final String flowExecutionName);

    /**
     * Gets execution report schema.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @return the execution report schema
     */
    String getExecutionReportSchema(final String flowId, final String flowExecutionName);

    /**
     * Suspend execution.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     */
    void suspendExecution(final String flowId, final String flowExecutionName);

    /**
     * Suspend all executions for flow version.
     *
     * @param flowId      the flow id
     * @param flowVersion the flow version
     */
    void suspendAllExecutionsForFlowVersion(final String flowId, final String flowVersion);

    /**
     * Stop execution.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     */
    void stopExecution(final String flowId, final String flowExecutionName);


    /**
     * Gets flow input schema and data.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @return the flow input schema and data
     */
    String getFlowInputSchemaAndData(final String flowId, final String flowExecutionName);

    /**
     * Gets execution resource.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @param resource          the resource
     * @return the execution resource
     */
    FlowExecutionResource getExecutionResource(final String flowId, final String flowExecutionName, String resource);

    /**
     * Gets execution events filtered by sepecified {@code filter}.
     *
     * @param flowExecutionEventFilter the flow execution event filter
     * @return the execution events
     */
    PaginatedData<FlowExecutionEvent> getExecutionEvents(FlowExecutionEventFilter flowExecutionEventFilter);

    /**
     * Returns the execution report variable.
     *
     * @param flowId            the id of the flow
     * @param flowExecutionName the name of the execution
     * @param variableName      the name of the variable
     * @return the {@code FlowExecutionResource} instance
     */
    FlowExecutionResource getExecutionReportVariable(final String flowId, final String flowExecutionName, final String variableName);
}